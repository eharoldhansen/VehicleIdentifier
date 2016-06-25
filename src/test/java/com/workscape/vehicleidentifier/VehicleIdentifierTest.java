package com.workscape.vehicleidentifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit test for simple App.
 */
public class VehicleIdentifierTest
{

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void testURL()
	{
		if (File.separatorChar == '/')
		{
			String fileUrl = VehicleIdentifier.convertToFileURL("/Users/ehansen/vid/input.xml");
			assertEquals(fileUrl, "file:/Users/ehansen/vid/input.xml");
		}
		else if (File.separatorChar == '\\')
		{
			String fileUrl = VehicleIdentifier.convertToFileURL("\\Users\\ehansen\\vid\\input.xml");
			assertEquals(fileUrl, "file:/C:/Users/ehansen/vid/input.xml");
		}
	}

	@Test
	public void testVehicleId()
	{

		Map<String, String> vehicle = new HashMap<String, String>();
		vehicle.put("wheelcount", "2");
		vehicle.put("framematerial", "metal");
		vehicle.put("powertrain", "human");
		vehicle.put("wheelmaterial", "metal");

		VehicleIdentifier vid = new VehicleIdentifier();
		String type = vid.identifyVehicle(vehicle);
		assertEquals("Bicycle", type);

		vehicle.put("powertrain", "internalCombustion");
		type = vid.identifyVehicle(vehicle);
		assertEquals("Motorcycle", type);

		vehicle.put("wheelcount", "4");
		type = vid.identifyVehicle(vehicle);
		assertEquals("Car", type);

		vehicle.put("wheelcount", "3");
		vehicle.put("framematerial", "plastic");
		vehicle.put("powertrain", "human");
		vehicle.put("wheelmaterial", "plastic");
		type = vid.identifyVehicle(vehicle);
		assertEquals("Big Wheel", type);

		vehicle.remove("wheelcount");
		//vehicle.put("framematerial", "plastic");
		vehicle.put("powertrain", "bernoulli");
		vehicle.remove("wheelmaterial");
		type = vid.identifyVehicle(vehicle);
		assertEquals("Hang Glider", type);

		vehicle.put("wheelcount", "1");
		type = vid.identifyVehicle(vehicle);
		assertEquals("Unidentified", type);

	}

	@Test
	public void loadTest() throws ParserConfigurationException, SAXException, IOException
	{
		String filePath = "src/main/resources/vehicles2.xml";
		String url = VehicleIdentifier.convertToFileURL(filePath);

		VehicleIdentifier vid = new VehicleIdentifier();
		vid.load(url);
		Map<String, String> details = vid.getDetail();

		assertEquals(4, details.size());
		assertEquals("Car", details.get("car 1"));
		assertEquals("Hang Glider", details.get("hang glider 1"));
		assertEquals("Unidentified", details.get("unidentified 1"));
		assertEquals("Unidentified", details.get("unidentified 2"));

		Map<String, Integer> summary = vid.getSummary();
		assertEquals(1, (int) summary.get("Car"));
		assertEquals(1, (int) summary.get("Hang Glider"));
		assertEquals(2, (int) summary.get("Unidentified"));
	}
}
