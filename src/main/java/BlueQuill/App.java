package BlueQuill;

import org.jsoup.*;
import org.jsoup.nodes.*;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * Hello world!
 *
 */
public class App 
{
	public static int TotalFilesProcessed = 0;
	public static int TotalJSP_INC_Files = 0;
	public static int TotalElementsWithTextFound = 0;
	public static int TotalElements = 0;
	public static int TotalElementsPerFile = 0;
	
	public static PrintWriter outputFile = null;
	public static List<String> ignoreFiles = Arrays.asList("styleguide.html".split(","));
	public static List<String> fileTypes = Arrays.asList("jsp,inc,html".split(","));
	
	public static boolean CREATE_OUTPUT_FILE = false;
	
    public static void main( String[] args ) throws Exception
    {

        System.out.println( "Hello World!" );
		String html = "<p>hello world</p><div>First parse<p>Parsed HTML into a doc.</p></div>";
	
		//receive directory to recursively look through for inc or jsp files		
		if(args.length < 1){
			throw new IllegalArgumentException("Need to provide the directory to search as a full absolute path");
		}
		
		System.out.println(args[0]);
		
		File file = new File(args[0]);
		if(!file.exists() || !file.isDirectory()){
			throw new IllegalArgumentException("Could not find "+file.getAbsolutePath()+" as an absolute path");
		}
		
		if(args.length == 2){
			CREATE_OUTPUT_FILE = new Boolean(args[1]);
		}
		
		outputFile = new PrintWriter("./output_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".txt");
		
		processDirectory(file);				
				
		for(int i=0; i<parsedFiles.size(); i++)
		{
			outputFile.print(parsedFiles.get(i));		
		}		
		
		
		outputFile.print(outputEndOfProcess());				
		System.out.print(outputEndOfProcess());
		
		outputFile.flush();
		
		/*
		for(int i=0; i<parsedFiles.size(); i++)
		{
			Comparator cmp = Collections.reverseOrder();
			Collections.sort(parsedFiles.get(i).parsedElements, cmp);
			for(int j=0; j<parsedFiles.get(i).parsedElements.size(); j++){
				//File search for lastIndex Of text
				String fileString = new String(Files.readAllBytes(Paths.get(parsedFiles.get(i).parsedElements.get(j).fileName)));
				//byte[] textBytes = parsedFiles.get(i).parsedElements.get(j).text.getBytes();
				int indexOfText = fileBytes.indexOf(parsedFiles.get(i).parsedElements.get(j).text);
				int indexPointer = indexOftext;
				boolean notFound = false;
				//List<String> lines = Files.readAllLines(Paths.get(parsedFiles.get(i).parsedElements.get(j).fileName), StandardCharsets.US_ASCII);
				
				while(notFound){
					int indexOfElementNode = fileString.substring(0, indexPointer).lastIndexOf(parsedFiles.get(i).parsedElements.get(j).element);
					
				}

				//starting at that index search backward for element tag/node  
				//if found closing tag, then count must find that many opening tags +1
				//the +1 is the element.... now find the end of the element  >
				// add to the end of the element the data-translation-id attribute
			
				System.out.println("Let's find this element at the end of the file... "+parsedFiles.get(i).parsedElements.get(j));
			}
			
		}
		*/
		
		
		outputFile.close();

    }
		
	public static List<ParsedFile> parsedFiles = new ArrayList<ParsedFile>();
	
	public static void processDirectory(File dir){
		File[] filesAndDirs = dir.listFiles();
		for(int i=0; i< filesAndDirs.length; i++){
			if(filesAndDirs[i].isDirectory()){
				if((filesAndDirs[i].getAbsolutePath().contains("ckfinder") && filesAndDirs[i].getAbsolutePath().contains("help")) ||
					(filesAndDirs[i].getAbsolutePath().contains("ckeditor") && filesAndDirs[i].getAbsolutePath().contains("lib"))){
					//skip ckfinder help directory
					return;
				}
				processDirectory(filesAndDirs[i]);
			}else{
				if(!ignoreFiles.contains(filesAndDirs[i].getName())){
					processFile(filesAndDirs[i]);
				}
			}
		}
	}
		
	public static String fileNameBeingParsed = null;	
	public static List<ParsedElement> parsedElements = new ArrayList<ParsedElement>();
	
	public static void processFile(File file){
		
		try{
			parsedElements = new ArrayList<ParsedElement>();
		
			TotalFilesProcessed++;
			TotalElementsPerFile = 0;
			fileNameBeingParsed = file.getAbsolutePath();
			String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());		
			if( fileTypes.contains(extension) ){
			
				ParsedFile parsedFile = new ParsedFile();
				parsedFile.fileName = file.getAbsolutePath();
			
				TotalJSP_INC_Files++;
				System.out.println("Started Parsing File...["+file.getAbsolutePath()+"]");
				
				String serverSideHTMLContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
				//for each jsp/inc file load it as a fragment document
				Document doc = Jsoup.parse(serverSideHTMLContent);

				Element bodyElement = doc.body();
				//recursively parse the text out of each element
				if(bodyElement != null){
					parseTextFromElement(bodyElement);
					//create new File With Added Attributes
					if(CREATE_OUTPUT_FILE){
						createFile(doc, file.getName());
					}
				} else {
					System.out.println("ERROR:: bodyElement was null");
				}
				
				parsedFile.parsedElements = parsedElements;
				parsedFiles.add(parsedFile);
								
				//save the text and the element name and the file name in a row			
				System.out.println("Finished Parsing File...["+file.getAbsolutePath()+"]");
			}else{
				System.out.println("Skipped Parsing File...["+file.getAbsolutePath()+"]");
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
	}			
	public static void createFile(Document document, String fileName){
		try{
			PrintWriter f = new PrintWriter(fileName);		
			f.print(document.toString());
			f.flush();
			f.close();
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
		}
	}
	
	public static void parseTextFromElement(Element el){
		TotalElements++;
		ParsedElement elementText = new ParsedElement();
		elementText.fileName = fileNameBeingParsed;
		elementText.element = ((el.nodeName() == null) ? "" : el.nodeName());
		elementText.elementId = ((el.id() == null) ? "" : el.id());
		elementText.text =  ((el.ownText() == null) ? "" : el.ownText());
		elementText.jsoupElement = el;
		elementText.elementClasses = ((el.classNames() == null) ? new HashSet<String>() : el.classNames());
		if(!ignoreTextPattern(elementText.text)){
			TotalElementsPerFile++;
			elementText.index = TotalElementsPerFile;
			parsedElements.add(elementText);
			TotalElementsWithTextFound++;	
			el.attr("data-translationId", Integer.toString(TotalElementsWithTextFound));
			el.addClass("translation");
		}		
		for(int i=0; i<el.children().size(); i++){
			parseTextFromElement(el.children().get(i));
		}		
	}
	
	public static boolean ignoreTextPattern(String text){
		return
		(
		 (text.startsWith("<%@") && text.endsWith("%>") && text.indexOf("page") > 0 && (text.indexOf("language") > 0 || text.indexOf("import") > 0)) ||
		 (text.replace(String.valueOf((char) 160), " ").trim().equals("")) ||
		 (text.startsWith("${") && text.endsWith("}") && text.trim().indexOf("}") == (text.length()-1)) ||
		 (text.startsWith("<%") && text.endsWith("%>") && (text.trim().indexOf("%>") == (text.length()-2) || text.indexOf("taglib") > 0))
		);
		 		    
	}
	
	public static String outputEndOfProcess(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Total Files Processed:"+TotalFilesProcessed+"\n");
		strBuffer.append("Total JSP/INC Files:"+TotalJSP_INC_Files+"\n");
		strBuffer.append("Elements:"+TotalElements+"\n");
		strBuffer.append("Elements With Text Found:"+TotalElementsWithTextFound+"\n");
		return strBuffer.toString();
	}
}

class ParsedElement implements Comparable<ParsedElement>
{
	public int index = 0;
	public String fileName;
	public String element;
	public Set<String> elementClasses;
	public Element jsoupElement;
	public String elementId;
	public String text;
	public String toString(){
		//return "{ fileName:\""+fileName + "\", element:\"" + element + "\", text:\"" + text + "\"}";
		//return fileName.replace(",", "@#@") + ", "+index+", " + element.replace(",", "@#@") + "," +elementId.replace(",", "@#@")+", "+ text.replace(",", "@#@");
		return fileName+"\t"+element+"\t"+elementId+"\t"+elementClasses+"\t"+text+"\t"+jsoupElement.toString().replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
		
	}
	public int compareTo(ParsedElement y){
		return this.compare(this, y);
	}
	public int compare(ParsedElement x, ParsedElement y) {
		return x.index < y.index ? -1
			 : x.index > y.index ? 1
			 : 0;	
	}
}
class ParsedFile
{
	public String fileName;
	public List<ParsedElement> parsedElements;
	public String toString(){
		StringBuffer strBuffer = new StringBuffer();
		for(int i=0; i<parsedElements.size(); i++){
			strBuffer.append(parsedElements.get(i).toString()+"\n");
		}
		return strBuffer.toString();		
	}	
}
