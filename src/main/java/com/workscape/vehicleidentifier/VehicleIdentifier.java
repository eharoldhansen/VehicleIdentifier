package com.workscape.vehicleidentifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Hello world!
 *
 */
public class VehicleIdentifier extends DefaultHandler
{
	private Map<String, Integer> summary;
	private Map<String, String> detail;
	private Map<String, String> currentVehicle;
	private int wheelCount;
	private String currentName;

	public VehicleIdentifier()
	{
		this.summary = new HashMap<>();
		this.detail = new HashMap<>();
		this.currentVehicle = new HashMap<>();
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
	{
		if (args.length < 1)
		{
			System.out.println("Usage: com.workscape.vehicleidentifier.VehicleIdentfier  <path to vehicle file>");
			System.exit(1);
		}
		
		String url = VehicleIdentifier.convertToFileURL(args[0]);

		VehicleIdentifier vid = new VehicleIdentifier();
		vid.load(url);

		Map<String, String> details = vid.getDetail();

		System.out.println("Details:");

		for (String key : details.keySet())
		{
			System.out.format("%1$-20s %2$-20s %n", key, details.get(key));
		}

		System.out.println("\nSummary:");
		Map<String, Integer> totals = vid.getSummary();

		for (String key : totals.keySet())
		{
			System.out.format("%1$5d %2$-20s %n", totals.get(key), key);
		}
	}

	public static String convertToFileURL(String filename)
	{
		String path = new File(filename).getAbsolutePath();
		if (File.separatorChar != '/')
		{
			path = path.replace(File.separatorChar, '/');
		}

		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		return "file:" + path;
	}

	public void load(String url) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();

		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(url);
	}

	public Map<String, Integer> getSummary()
	{
		return summary;
	}

	public Map<String, String> getDetail()
	{
		return detail;
	}

	@Override
	public void startElement(String namespaceURI,
			String localName,
			String qName,
			Attributes atts)
			throws SAXException
	{

		if ("frame".equals(currentName))
		{
			currentName += localName;
		}
		else if ("wheel".equals(currentName))
		{
			if ("material".equals(localName))
			{
				currentName += localName;
			}
		}
		else
		{
			currentName = localName;
		}
	}

	@Override
	public void endElement(String uri,
			String localName,
			String qName)
			throws SAXException
	{
		if (null != localName)
		switch (localName)
		{
			case "vehicle":
				identifyVehicle(currentVehicle);
				currentVehicle.clear();
				break;
			case "wheel":
				wheelCount++;
				break;
			case "wheels":
				currentVehicle.put("wheelcount", Integer.toString(wheelCount));
				wheelCount = 0;
				break;
			case "human":
			case "internalCombustion":
			case "bernoulli":
				currentVehicle.put("powertrain", localName);
				break;
		}
	}

	@Override
	public void characters(char[] ch,
			int start,
			int length)
	{
		String value = null;

		if (null != currentName)
		switch (currentName)
		{
			case "id":
				value = new String(ch, start, length).trim();
				saveValue(currentName, value);
				break;
			case "framematerial":
				value = new String(ch, start, length).trim();
				saveValue(currentName, value);
				break;
			case "wheelmaterial":
				value = new String(ch, start, length).trim();
				saveValue(currentName, value);
				break;
		}
	}

	public String identifyVehicle(Map<String,String> vehicle)
	{
		String type = "Unidentified";

		Integer wheelcount = vehicle.get("wheelcount") != null ? Integer.parseInt(vehicle.get("wheelcount")) : null;
		if (wheelcount != null)
		{
			if (wheelcount == 3)
			{
				if ("plastic".equals(vehicle.get("wheelmaterial"))
						&& "plastic".equals(vehicle.get("framematerial"))
						&& "human".equals(vehicle.get("powertrain")))
				{
					type = "Big Wheel";
				}
			}
			else if (wheelcount == 4)
			{
				if ("metal".equals(vehicle.get("framematerial"))
						&& "internalCombustion".equals(vehicle.get("powertrain")))
				{
					type = "Car";
				}
			}
			else if (wheelcount == 2)
			{
				if ("human".equals(vehicle.get("powertrain")))
				{
					if ("metal".equals(vehicle.get("wheelmaterial"))
							&& "metal".equals(vehicle.get("framematerial")))
					{
						type = "Bicycle";
					}
				}
				else if ("internalCombustion".equals(vehicle.get("powertrain")))
				{
					if ("metal".equals(vehicle.get("wheelmaterial"))
							&& "metal".equals(vehicle.get("framematerial")))
					{
						type = "Motorcycle";
					}
				}
			}
		}
		else
		{
			if ("plastic".equals(vehicle.get("framematerial"))
					&& "bernoulli".equals(vehicle.get("powertrain")))
			{
				type = "Hang Glider";
			}
		}

		tally(type);

		detail.put(vehicle.get("id"), type);
		
		return type;
	}

	private void tally(String type)
	{
		Integer count = summary.get(type);

		if (count == null)
		{
			count = new Integer(1);
		}
		else
		{
			count++;
		}
		summary.put(type, count);
	}

	private void saveValue(String key, String s)
	{
		if (s == null || s.isEmpty())
		{
			return;
		}

		String value = currentVehicle.get(key);

		if (s.equals(value))
		{
			return;
		}

		if (value != null && !value.isEmpty())
		{
			value += s;
		}
		else
		{
			value = s;
		}
		currentVehicle.put(key, value);
	}
}
