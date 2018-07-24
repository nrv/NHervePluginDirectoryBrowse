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

import java.sql.SQLException;

public class H2DBWrapper extends DBWrapper {
	private final static String DRIVER = "org.h2.Driver";
	private final static String URL = "jdbc:h2:";
	
	@Override
	protected String getDriver() {
		return DRIVER;
	}
	
	@Override
	protected String getUrl() {
		return URL + getDbDirectory() + "/" + getDbName();
	}

	@Override
	protected void postConnect() throws SQLException {
		info("[H2DBWrapper] using directory : " + getDbDirectory());
	}
	
	@Override
	protected void postDisconnect() throws SQLException {
		
	}

	@Override
	protected void preConnect() throws SQLException {
		
	}

	@Override
	protected void preDisconnect() throws SQLException {
	}

	@Override
	public String getName() throws SQLException {
		return "H2";
	}


}
