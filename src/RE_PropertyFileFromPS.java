

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RE_PropertyFileFromPS 
{
	static String PROPERTY_FILE_DIR = "E:\\Finanicial Force\\FFA\\source\\src\\permissionsetProperty\\";
	static String PERMISSION_SET__DIR = "E:\\Old Permission Sets\\New folder";
	
	public static void main(String[] ar)
	{
		File propertyFileDir = new File(PERMISSION_SET__DIR);
		for(File propertyFile:propertyFileDir.listFiles())
		{
			if(propertyFile.isDirectory())
				parsePSFile(propertyFile);
		}
	}

	public static void parsePSFile(File propertyFileDir)
	{
		BufferedReader br = null;
		ArrayList<FieldProperty> lines = new ArrayList<FieldProperty>();
		String nListLabel="";
		String peopertyFileContent="";
		
		try 
		{
			for(File fXmlFile:propertyFileDir.listFiles())
			{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();	

				peopertyFileContent += "#Start"; 

				nListLabel = doc.getElementsByTagName("label").item(0).getTextContent();
			
				if(nListLabel.contains("- Read Access"))
				{
					nListLabel =nListLabel.replace("Read Access", "Template");
				}
				else if(nListLabel.contains("Read Access"))
				{
					nListLabel =nListLabel.replace("Read Access", "- Template");
				}
				
				peopertyFileContent += "\npermissionSetName"+"="+ nListLabel;

				
				NodeList nList = doc.getElementsByTagName("objectPermissions");

				for (int temp = 0; temp < nList.getLength(); temp++) 
				{

					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						String permissionString="";
						Element eElement = (Element) nNode;

						if(eElement.getElementsByTagName("allowCreate").item(0).getTextContent().equalsIgnoreCase("true"))
							permissionString +="C";
						if(eElement.getElementsByTagName("allowRead").item(0).getTextContent().equalsIgnoreCase("true"))
							permissionString +="R";
						if(eElement.getElementsByTagName("allowEdit").item(0).getTextContent().equalsIgnoreCase("true"))
							permissionString +="U";
						if(eElement.getElementsByTagName("allowDelete").item(0).getTextContent().equalsIgnoreCase("true"))
							permissionString +="D";					

						String objName = eElement.getElementsByTagName("object").item(0).getTextContent();
						if(!objName.startsWith("fflib"))
						{
							peopertyFileContent += "\nobjectPermissions"+"="+objName+"#"+permissionString;
						}	
					}
				}


				NodeList nListPage = doc.getElementsByTagName("pageAccesses");

				for (int temp = 0; temp < nListPage.getLength(); temp++) 
				{

					Node nNode = nListPage.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element eElement = (Element) nNode;

						String apexPageName = eElement.getElementsByTagName("apexPage").item(0).getTextContent();

						peopertyFileContent += "\npageAccesses"+"="+ apexPageName;
					}
				}


				NodeList nListTab = doc.getElementsByTagName("tabSettings");

				for (int temp = 0; temp < nListTab.getLength(); temp++) 
				{

					Node nNode = nListTab.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element eElement = (Element) nNode;


						String tabName = eElement.getElementsByTagName("tab").item(0).getTextContent();
						String vibility = "";
						if(eElement.getElementsByTagName("visibility").item(0).getTextContent().equalsIgnoreCase("Available"))
						{
							vibility="A";
							peopertyFileContent += "\ntabSettings"+"="+ tabName+"#"+vibility;
						}
						else if(eElement.getElementsByTagName("visibility").item(0).getTextContent().equalsIgnoreCase("Visible"))
						{
							vibility="V";
							peopertyFileContent += "\ntabSettings"+"="+ tabName+"#"+vibility;
						}

					}


				}

				NodeList nListClasses = doc.getElementsByTagName("classAccesses");

				for (int temp = 0; temp < nListClasses.getLength(); temp++) 
				{

					Node nNode = nListClasses.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element eElement = (Element) nNode;

						String apexClassName = eElement.getElementsByTagName("apexClass").item(0).getTextContent();
						peopertyFileContent += "\nclassAccesses"+"="+ apexClassName;
					}
				}
				peopertyFileContent += "\n#End\n\n";
			}

		
			File file = new File(PROPERTY_FILE_DIR+nListLabel.split("-")[1].trim().replace("Read Access","").trim()+".properties");	
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(peopertyFileContent);
			bw.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try
			{
				if (br != null)br.close();
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		}

		Collections.sort(lines);

	}
}	
	
	
	
