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

import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDBWrapper extends DBWrapper {
	private final static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private final static String URL_P1 = "jdbc:derby:";
	private final static String URL_P2 = ";create=true";
	private final static String CLOSEURL = "jdbc:derby:;shutdown=true";

	@Override
	protected String getDriver() {
		return DRIVER;
	}

	@Override
	protected String getUrl() {
		return URL_P1 + getDbName() + URL_P2;
	}

	@Override
	protected void postConnect() throws SQLException {
		info("[DerbyDBWrapper] using directory : " + getDbDirectory());
	}

	@Override
	protected void postDisconnect() throws SQLException {
		try {
			DriverManager.getConnection(CLOSEURL);
		} catch (SQLException se) {
			if (!se.getSQLState().equals("XJ015")) {
				throw se;
			}
		}
	}

	@Override
	protected void preConnect() throws SQLException {
		System.setProperty("derby.system.home", getDbDirectory());
	}

	@Override
	protected void preDisconnect() throws SQLException {
	}

	@Override
	public String getName() throws SQLException {
		return "Derby";
	}
}
