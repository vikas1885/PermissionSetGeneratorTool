import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class PermissionSetGenerator 
{
	public static final String BASE_DIR = "E:\\Finanicial Force\\FFA\\source\\src\\";
	public static final String PS_DIR = BASE_DIR + "permissionsets" + File.separatorChar;
	public static final String PS_TEMPLATE_DIR = BASE_DIR + "permissionset_templates";
	public static final String PS_PROPERTY_DIR = BASE_DIR + "permissionsetProperty";
	public static final String OBJECTS_DIR = BASE_DIR + "objects";

	public static final String COLUMN_SEPERATOR = "=";
	public static final String PERMISSION_SEPERATOR = "#";
	public static final String PERMISSION_SET_NAME = "permissionSetName";

	public static final String NODE_CLASS_ACCESSES = "classAccesses";
	public static final String NODE_TAB_SETTINGS = "tabSettings";
	public static final String NODE_PAGE_ACCESSES = "pageAccesses";
	public static final String NODE_OBJECT_PERMISSION = "objectPermissions";
	public static final String NODE_TAB = "tab";
	public static final String NODE_APEX_PAGE = "apexPage";
	public static final String NODE_APEX_CLASS = "apexClass";
	public static final String NODE_LABEL = "label";
	public static final String NODE_FIELD_PERMISSIONS = "fieldPermissions";
	public static final String NODE_FIELDS = "fields";

	
	public static final String ELM_ALLOW_CREATE = "allowCreate";
	public static final String ELM_ALLOW_DELETE = "allowDelete";
	public static final String ELM_ALLOW_EDIT = "allowEdit";
	public static final String ELM_ALLOW_READ = "allowRead";
	public static final String ELM_VIEW_ALL_RECORDS = "viewAllRecords";
	public static final String ELM_MODIFY_ALL_RECORDS = "modifyAllRecords";
	public static final String ELM_VISIBILITY = "visibility";
	public static final String ELM_EDITABLE = "editable";
	public static final String ELM_FIELD = "field";
	public static final String ELM_READABLE = "readable";
	public static final String ELM_ENABLED = "enabled";
	public static final String ELM_OBJECT = "object";
	
	
	public static final String ELM_VAL_TRUE = "true";
	public static final String ELM_VAL_FALSE = "false";
	public static final String ELM_VAL_VISIBLE = "Visible";
	public static final String ELM_VAL_AVAILABLE = "Available";

	public static void main(String[] args) throws Exception
	{
		File propertyFileDir = new File(PS_PROPERTY_DIR);
		List<PermissionSet>	permissionSetList = new ArrayList<PermissionSet>();

		for(File propertyFile:propertyFileDir.listFiles())
		{
			if(propertyFile.getName().endsWith(".properties"))
			{
				System.out.println("Processing  "+ propertyFile.getName());
				permissionSetList.addAll(parsePropertyFile(propertyFile));
			}	
		}
		createPermissionSetFiles(permissionSetList);
	}

	public static void createPermissionSetFiles(List<PermissionSet> permissionSetList)
	{	
		if(permissionSetList != null)
			for(PermissionSet ps:permissionSetList)
				createPermissionSetFile(ps);
	}


	public static void createNode(Document doc, Element rootElement,String parentNode, Map<String,String> nodes)
	{
		Element rootElement1 = doc.createElement(parentNode);
		rootElement.appendChild(rootElement1);
		for(String nodeName:nodes.keySet())
		{
			Element elm = doc.createElement(nodeName);
			elm.setTextContent(nodes.get(nodeName));
			rootElement1.appendChild(elm);
		}	
	}


	public static void createPermissionSetFile(PermissionSet ps)
	{
		try 
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
	    	doc.setXmlStandalone(true);
		
	    	Element rootElement = doc.createElement("PermissionSet");
			doc.appendChild(rootElement);

			Attr attr = doc.createAttribute("xmlns");
			attr.setValue("http://soap.sforce.com/2006/04/metadata");
			rootElement.setAttributeNode(attr);

			for(String apexClassName:ps.getApexClasses())
			{
				Map<String,String> nodeValueMap = new TreeMap<String,String>();

				nodeValueMap.put(NODE_APEX_CLASS, apexClassName);
				nodeValueMap.put(ELM_ENABLED,ELM_VAL_TRUE);

				createNode(doc,rootElement,NODE_CLASS_ACCESSES,nodeValueMap);
			}
			
			for(String object:ps.getObjects().keySet())
			{
				Map<String,String> nodeValueMap = new TreeMap<String,String>();
				String permission = ps.getObjects().get(object);

				
				for(FieldProperty fp:getFields(object))
				{
					if(fp.getFormulaField())
						nodeValueMap.put(ELM_EDITABLE,ELM_VAL_FALSE);
					else
						nodeValueMap.put(ELM_EDITABLE,ELM_VAL_TRUE);

					nodeValueMap.put(ELM_READABLE,ELM_VAL_TRUE);
					nodeValueMap.put(ELM_FIELD, fp.getFieldName());
					if(permission.equalsIgnoreCase("R"))
					{
						nodeValueMap.put(ELM_EDITABLE,ELM_VAL_FALSE);
					}	

					createNode(doc,rootElement,NODE_FIELD_PERMISSIONS,nodeValueMap);
				}
			}

			Element labelElement = doc.createElement(NODE_LABEL);
			labelElement.setTextContent(ps.getPermissionSetName());

			rootElement.appendChild(labelElement);

			for(String object:ps.getObjects().keySet())
			{
				String permission = ps.getObjects().get(object).toUpperCase();
				Map<String,String> nodeValueMap = new TreeMap<String,String>();

				if(permission.contains("C"))
					nodeValueMap.put(ELM_ALLOW_CREATE,ELM_VAL_TRUE);
				else
					nodeValueMap.put(ELM_ALLOW_CREATE,ELM_VAL_FALSE);

				if(permission.contains("D"))
					nodeValueMap.put(ELM_ALLOW_DELETE,ELM_VAL_TRUE);
				else
					nodeValueMap.put(ELM_ALLOW_DELETE,ELM_VAL_FALSE);

				if(permission.contains("U"))
					nodeValueMap.put(ELM_ALLOW_EDIT,ELM_VAL_TRUE);
				else
					nodeValueMap.put(ELM_ALLOW_EDIT,ELM_VAL_FALSE);

				if(permission.contains("R"))
					nodeValueMap.put(ELM_ALLOW_READ,ELM_VAL_TRUE);
				else
					nodeValueMap.put(ELM_ALLOW_READ,ELM_VAL_FALSE);

				nodeValueMap.put(ELM_OBJECT,object);

				nodeValueMap.put(ELM_MODIFY_ALL_RECORDS,ELM_VAL_FALSE);
				nodeValueMap.put(ELM_VIEW_ALL_RECORDS,ELM_VAL_FALSE);

				createNode(doc,rootElement,NODE_OBJECT_PERMISSION,nodeValueMap);
			}

			for(String vfPage:ps.getVfPage())
			{
				Map<String,String> nodeValueMap = new TreeMap<String,String>();

				nodeValueMap.put(NODE_APEX_PAGE, vfPage);
				nodeValueMap.put(ELM_ENABLED,ELM_VAL_TRUE);

				createNode(doc,rootElement,NODE_PAGE_ACCESSES,nodeValueMap);
			}

			for(String tabName:ps.getTabs().keySet())
			{
				String visibilityType = ps.getTabs().get(tabName).toUpperCase();
				Map<String,String> nodeValueMap = new TreeMap<String,String>();

				nodeValueMap.put(NODE_TAB, tabName);

				if(visibilityType.contains("A"))
					nodeValueMap.put(ELM_VISIBILITY,ELM_VAL_AVAILABLE);
				else if(visibilityType.contains("V"))
					nodeValueMap.put(ELM_VISIBILITY,ELM_VAL_VISIBLE);

				createNode(doc,rootElement,NODE_TAB_SETTINGS,nodeValueMap);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			

			DOMSource source = new DOMSource(doc);
			String psName = ps.getPermissionSetName().replace(" ", "");
			String psPath = "";
			StreamResult result;

			if(psName.toUpperCase().endsWith("-TEMPLATE"))
			{
				psPath = PS_TEMPLATE_DIR + File.separatorChar + psName.replace("-Template","").replace("-", "") + ".permissionset_template";
			}
			else
			{
				psPath = PS_DIR + psName.replace("-", "") + ".permissionset";
			}
			result = new StreamResult(new File(psPath));
			
			transformer.transform(source, result);
			
			System.out.println(PS_DIR +"   " +ps.getPermissionSetName() + "  saved!");
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	public static ArrayList<FieldProperty> getFields(String objectName)
	{
		BufferedReader br = null;
		ArrayList<FieldProperty> lines = new ArrayList<FieldProperty>();
		FieldProperty fp;
		try 
		{
			File objectXMLFile = new File(BASE_DIR + "objects"+ File.separatorChar + objectName + ".object");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(objectXMLFile);
			doc.getDocumentElement().normalize();	

			NodeList nList = doc.getElementsByTagName("fields");

			for (int temp = 0; temp < nList.getLength(); temp++) 
			{

				fp = new FieldProperty(); 
				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;

					fp.setFieldName(objectName+"."+ eElement.getElementsByTagName("fullName").item(0).getTextContent());

					fp.setType(eElement.getElementsByTagName("type").item(0).getTextContent());

					if(eElement.getElementsByTagName("formula").item(0) != null || eElement.getElementsByTagName("summarizedField").item(0) != null || eElement.getElementsByTagName("MasterDetail").item(0) != null)
					{
						fp.setFormulaField(true);
					}

					if(!fp.getType().equalsIgnoreCase("MasterDetail") && !(eElement.getElementsByTagName("required").item(0)!=null && eElement.getElementsByTagName("required").item(0).getTextContent().equalsIgnoreCase(ELM_VAL_TRUE)))
					{
						lines.add(fp);
					}
				}
			}
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

		return lines;
	}

	public static List<PermissionSet> parsePropertyFile(File propertyFile) throws Exception
	{
		BufferedReader br = null;
		String sCurrentLine = null;
		List<PermissionSet> permissionSetList = new ArrayList<PermissionSet>();

		try 
		{
			br = new BufferedReader(new FileReader(propertyFile));

			while ((sCurrentLine = br.readLine()) != null)
			{
				sCurrentLine = sCurrentLine.trim();
						
				if(sCurrentLine.equalsIgnoreCase("#Start"))
				{
					sCurrentLine = br.readLine().trim();
					
					PermissionSet psProperty = new PermissionSet();

					while(sCurrentLine != null && !sCurrentLine.equalsIgnoreCase("#End"))
					{
						if(!sCurrentLine.isEmpty())
						{
							String columns[] = sCurrentLine.split(COLUMN_SEPERATOR);

							if(columns.length == 2)
							{
								String cstic = columns[0].trim();
								String value = columns[1].trim();

								if(cstic.isEmpty()||value.isEmpty())
								{
									throw new Exception(propertyFile.getName() + "-->Error in Line:- " + sCurrentLine + "\nIt was expected to have 2 colums in line");
								}
								else
								{	
									if(cstic.equalsIgnoreCase(NODE_OBJECT_PERMISSION))
									{
										String[] objectPermission= value.split(PERMISSION_SEPERATOR);
										psProperty.getObjects().put(objectPermission[0].trim(),objectPermission[1].trim().toUpperCase());
									}
									else if(cstic.equalsIgnoreCase(NODE_PAGE_ACCESSES))
									{
										psProperty.getVfPage().add(value);						
									}
									else if(cstic.equalsIgnoreCase(NODE_TAB_SETTINGS))
									{
										String[] tabVisiblity = value.split(PERMISSION_SEPERATOR);
										psProperty.getTabs().put(tabVisiblity[0].trim(),tabVisiblity[1].trim().toUpperCase());
									}
									else if(cstic.equalsIgnoreCase(NODE_CLASS_ACCESSES))
									{
										psProperty.getApexClasses().add(value);
									}
									else if(cstic.equalsIgnoreCase(PERMISSION_SET_NAME))
									{
										psProperty.setPermissionSetName(value);
									}
									else
									{
										throw new Exception(propertyFile.getName() + "-->Error in Line:-" + sCurrentLine + "\nInvalid Property:-" + cstic);
									}
								}
							}
							else
							{
								throw new Exception(propertyFile.getName() + "-->Error in Line:- " + sCurrentLine + "\nIt was expected to have 2 colums in line \n" + sCurrentLine);
							}
						}
						sCurrentLine = br.readLine();
					}
					permissionSetList.add(psProperty);
				}	
			}
		} 
		catch(ArrayIndexOutOfBoundsException ea)
		{
			throw new Exception(propertyFile.getName() + "-->Error in Line:- "  + sCurrentLine + "\nIncorrect Format");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				if (br != null)br.close();
			} 
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		}
		return permissionSetList;
	}

}


class PermissionSet
{

	Map<String,String> objects;
	Map<String,String> tabs;
	ArrayList<String> vfPage;
	ArrayList<String> apexClasses;
	String permissionSetName;

	PermissionSet()
	{
		objects = new TreeMap<String,String>();
		tabs = new TreeMap<String,String>();
		vfPage = new ArrayList<String>();
		apexClasses = new ArrayList<String>();
	}

	public Map<String,String> getObjects() 
	{
		return objects;
	}

	public Map<String,String> getTabs() 
	{
		return tabs;
	}

	public ArrayList<String> getVfPage() {
		return vfPage;
	}
	
	public ArrayList<String> getApexClasses() {
		return apexClasses;
	}

	public String getPermissionSetName() {
		return permissionSetName;
	}

	public void setObjects(Map<String,String> objects) {
		this.objects = objects;
	}
	public void setTabs(Map<String,String> tabs) {
		this.tabs = tabs;
	}
	public void setVfPage(ArrayList<String> vfPage) {
		this.vfPage = vfPage;
	}
	public void setApexClasses(ArrayList<String> apexClasses) {
		this.apexClasses = apexClasses;
	}
	public void setPermissionSetName(String permissionSetName) {
		this.permissionSetName = permissionSetName;
	}
}

class FieldProperty implements Comparable<FieldProperty>
{
	String fieldName,type;
	Boolean formulaField=false;

	public Boolean getFormulaField() {
		return formulaField;
	}

	public void setFormulaField(Boolean formulaField) {
		this.formulaField = formulaField;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int compareTo(FieldProperty fp) {

		return (this.fieldName).compareTo(fp.fieldName);
	}  
}
