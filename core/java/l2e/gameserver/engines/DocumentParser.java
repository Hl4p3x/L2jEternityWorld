/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e.gameserver.engines;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import l2e.Config;
import l2e.util.file.filter.XMLFilter;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public abstract class DocumentParser
{
	protected final Logger _log = Logger.getLogger(getClass().getName());
	
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	private static final XMLFilter xmlFilter = new XMLFilter();
	
	private File _currentFile;
	
	private Document _currentDocument;
	
	private FileFilter _currentFilter = null;
	
	public abstract void load();
	
	protected void parseDatapackFile(String path)
	{
		parseFile(new File(Config.DATAPACK_ROOT, path));
	}
	
	protected void parseFile(File f)
	{
		if (!getCurrentFileFilter().accept(f))
		{
			_log.warning(getClass().getSimpleName() + ": Could not parse " + f.getName() + " is not a file or it doesn't exist!");
			return;
		}
		
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(true);
		dbf.setIgnoringComments(true);
		_currentDocument = null;
		_currentFile = f;
		try
		{
			dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			final DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new XMLErrorHandler());
			_currentDocument = db.parse(f);
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Could not parse " + f.getName() + " file: " + e.getMessage());
			return;
		}
		parseDocument();
	}
	
	public File getCurrentFile()
	{
		return _currentFile;
	}
	
	protected Document getCurrentDocument()
	{
		return _currentDocument;
	}
	
	protected boolean parseDirectory(File file)
	{
		return parseDirectory(file, false);
	}
	
	protected boolean parseDirectory(String path)
	{
		return parseDirectory(new File(path), false);
	}
	
	protected boolean parseDirectory(String path, boolean recursive)
	{
		return parseDirectory(new File(path), recursive);
	}
	
	protected boolean parseDirectory(File dir, boolean recursive)
	{
		if (!dir.exists())
		{
			_log.warning(getClass().getSimpleName() + ": Folder " + dir.getAbsolutePath() + " doesn't exist!");
			return false;
		}
		
		final File[] listOfFiles = dir.listFiles();
		for (File f : listOfFiles)
		{
			if (recursive && f.isDirectory())
			{
				parseDirectory(f, recursive);
			}
			else if (getCurrentFileFilter().accept(f))
			{
				parseFile(f);
			}
		}
		return true;
	}
	
	protected void parseDocument(Document doc)
	{
	}
	
	protected abstract void parseDocument();
	
	protected static int parseInt(NamedNodeMap n, String name)
	{
		return Integer.parseInt(n.getNamedItem(name).getNodeValue());
	}
	
	protected static Integer parseInteger(NamedNodeMap n, String name)
	{
		return Integer.valueOf(n.getNamedItem(name).getNodeValue());
	}
	
	protected static int parseInt(Node n)
	{
		return Integer.parseInt(n.getNodeValue());
	}
	
	protected static Integer parseInteger(Node n)
	{
		return Integer.valueOf(n.getNodeValue());
	}
	
	protected Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue)
	{
		return parseInteger(attrs.getNamedItem(name), defaultValue);
	}
	
	protected Integer parseInteger(Node node, Integer defaultValue)
	{
		return node != null ? Integer.valueOf(node.getNodeValue()) : defaultValue;
	}
	
	protected static Long parseLong(NamedNodeMap n, String name)
	{
		return Long.valueOf(n.getNamedItem(name).getNodeValue());
	}
	
	protected static float parseFloat(NamedNodeMap n, String name)
	{
		return Float.parseFloat(n.getNamedItem(name).getNodeValue());
	}
	
	protected static Double parseDouble(NamedNodeMap n, String name)
	{
		return Double.valueOf(n.getNamedItem(name).getNodeValue());
	}
	
	protected static boolean parseBoolean(NamedNodeMap n, String name)
	{
		final Node b = n.getNamedItem(name);
		return (b != null) && Boolean.parseBoolean(b.getNodeValue());
	}
	
	protected static String parseString(NamedNodeMap n, String name)
	{
		final Node b = n.getNamedItem(name);
		return (b == null) ? "" : b.getNodeValue();
	}
	
	public void setCurrentFileFilter(FileFilter filter)
	{
		_currentFilter = filter;
	}
	
	public FileFilter getCurrentFileFilter()
	{
		return _currentFilter != null ? _currentFilter : xmlFilter;
	}
	
	protected class XMLErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void error(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void fatalError(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
	}
}