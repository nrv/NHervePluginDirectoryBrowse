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

package plugins.nherve.browser.cache.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import plugins.nherve.browser.cache.DBWrapper;
import plugins.nherve.browser.cache.DBWrapper.DBType;
import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.HashToolbox;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//File f = new File("D:/install/jpg1.tar/jpg/100000.jpg");
		File f = new File("/Users/nherve/Travail/subsets/3/004023.jpg");
		
		DBWrapper db = DBWrapper.create(DBType.H2);
		db.setLogEnabled(true);
		try {
			db.connect();
			db.tableExists();
			Algorithm.out("Size : " + db.tableSize());
			
			db.tableClear();
			
			BufferedImage bi = ImageIO.read(f);
			String hash = HashToolbox.hashMD5(f);
			
			db.insert(hash, bi);
			
			Algorithm.out("Size : " + db.tableSize());
			
			BufferedImage img = db.select(hash);
			
			Algorithm.out("Image info : " + img.getWidth() + " x " + img.getHeight());
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				db.disconnect();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
