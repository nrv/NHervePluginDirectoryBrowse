/*
 * Copyright 2011 Institut Pasteur.
 * 
 * This file is part of Image Browser, which is an ICY plugin.
 * 
 * Image Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Image Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Image Browser. If not, see <http://www.gnu.org/licenses/>.
 */

package plugins.nherve.browser.cache;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import plugins.nherve.toolbox.Algorithm;

public abstract class DBWrapper extends Algorithm {
	public enum DBType {DERBY, H2};
	
	public static DBWrapper create(DBType t) {
		switch (t) {
		case DERBY:
			return new DerbyDBWrapper();
		case H2:
			return new H2DBWrapper();
		default:
			return null;
		}
	}
	
	public static DBWrapper create(DBType t, String dbName) {
		DBWrapper wrap = create(t);
		wrap.dbName = dbName;
		
		return wrap;
	}
	
	public static DBWrapper create(DBType t, String tableName, String dbName, String dbDirectory) {
		DBWrapper wrap = create(t, dbName);
		wrap.tableName = tableName;
		wrap.dbDirectory = dbDirectory;
		
		return wrap;
	}

	private String tableName = "images";
	private String dbName = "imagedb";
	private String dbDirectory = System.getProperty("java.io.tmpdir");
	
	protected Connection conn;
	
	protected DBWrapper() {
		super();
	}

	public void connect() throws SQLException {
		try {
			preConnect();
			
			Class.forName(getDriver());
			conn = DriverManager.getConnection(getUrl());
			if (!tableExists()) {
				tableCreate();
			}
			
			postConnect();
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	}
	public void disconnect() throws SQLException {
		preDisconnect();
		
		SQLException exceptionOnClose = null;
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				exceptionOnClose = e;
			}
			conn = null;
		}

		postDisconnect();
		
		if (exceptionOnClose != null) {
			throw exceptionOnClose;
		}
	}
	
	public String getDbDirectory() {
		return dbDirectory;
	}
	public String getDbName() {
		return dbName;
	}
	
	protected abstract String getDriver();
	
	public String getTableName() {
		return tableName;
	}
	
	protected abstract String getUrl();

	public void insert(String h, BufferedImage t) throws SQLException {
		PreparedStatement s = null;
		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(t, "JPG", bos);
			bos.flush();
			byte[] ser = bos.toByteArray();
			bos.close();
			ByteArrayInputStream is = new ByteArrayInputStream(ser);

			s = conn.prepareStatement("insert into " + getTableName() + " values (?, ?)");
			s.setString(1, h);
			s.setBinaryStream(2, is);

			s.executeUpdate();
		} catch (IOException e) {
			throw new SQLException(e);
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	protected abstract void preDisconnect() throws SQLException;
	
	protected abstract void postDisconnect() throws SQLException;

	protected abstract void preConnect() throws SQLException;
	
	protected abstract void postConnect() throws SQLException;

	public BufferedImage select(String h) throws SQLException {
		PreparedStatement s = null;
		ResultSet r = null;
		try {
			s = conn.prepareStatement("select thumb from " + getTableName() + " where hash=?");
			s.setString(1, h);
			r = s.executeQuery();
			Blob b = null;
			if (r.next()) {
				b = r.getBlob(1);
				if (b != null) {
					InputStream is = b.getBinaryStream();
					BufferedImage bi = ImageIO.read(is);
					is.close();
					return bi;
				}
			}
			return null;
		} catch (IOException e) {
			throw new SQLException(e);
		} finally {
			if (r != null) {
				r.close();
			}
			if (s != null) {
				s.close();
			}
		}
	}

	public void tableClear() throws SQLException {
		Statement s = null;
		try {
			log("[DBWrapper] clearing everything");
			s = conn.createStatement();
			s.execute("truncate table " + getTableName());
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	public void tableCreate() throws SQLException {
		Statement s = null;
		try {
			log("[DBWrapper] creating table structure");
			s = conn.createStatement();
			s.execute("create table " + getTableName() + "(hash varchar(32) not null primary key, thumb blob(16M))");
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	public boolean tableExists() throws SQLException {
		Statement s = null;
		try {
			s = conn.createStatement();
			s.execute("select count(*) from " + getTableName());
			return true;
		} catch (SQLException sqle) {
			return false;
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	public long tableSize() throws SQLException {
		Statement s = null;
		ResultSet r = null;
		try {
			s = conn.createStatement();
			r = s.executeQuery("select sum(length(thumb)) from " + getTableName());
			long sz = 0;
			if (r.next()) {
				sz = r.getLong(1);
			}
			return sz;
		} finally {
			if (r != null) {
				r.close();
			}
			if (s != null) {
				s.close();
			}
		}
	}

	
}
