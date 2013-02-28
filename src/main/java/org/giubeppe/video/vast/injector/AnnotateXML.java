package org.giubeppe.video.vast.injector;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AnnotateXML {
	
	static Logger logger = Logger.getAnonymousLogger(); 
	Document doc;
	
	public static void main(String[] args) throws Exception {

		String filepath = "liverail.xml";

		AnnotateXML annotator = new AnnotateXML();
		annotator.parse(filepath);
		annotator.addBeacons();
		annotator.dumpFile();

	}

	private void dumpFile() throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("/tmp/output.xml"));
		transformer.transform(source, result);
	}

	private void parse(String filepath)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.parse(filepath);
	}
	
	private void addBeacons() {
		// looking for VAST
		Node vastNode = doc.getFirstChild();
		if (vastNode != null && vastNode.getNodeName().toLowerCase().equals("vast")) {
			NodeList adNodes = vastNode.getChildNodes();
			for (int i=0; i<adNodes.getLength(); i++) {
				Node adNode = adNodes.item(i);
				logger.info("the node name is: "+adNode.getNodeName());
			}
		}
		
		// look for a wrapper
		NodeList wrapperEls = doc.getElementsByTagName("AdWrapper");
		if (wrapperEls != null &&wrapperEls.getLength() > 0) {
			// this a wrapper
			logger.info("Wrapper found");
			Node wrapperNode = null;
			for (int i=0; i<wrapperEls.getLength(); i++) {
				addBeaconsToWrapper(wrapperEls.item(i));
			}
		} else {
			addBeaconsToVAST(doc);
		}
	}
	
	private void addBeaconsToVAST(Document doc) {
		// look for impressions
		NodeList impressionNodes = doc.getElementsByTagName("Impression");
		int impressionsNumber =impressionNodes.getLength(); 
		if (impressionsNumber > 0) {
			Node lastImpressionNode = impressionNodes.item(impressionsNumber-1);
			// TODO: this is actually embedding the impression inside the other impression.
			// we want to attach next to it
			lastImpressionNode.appendChild(createImpression());
		}
	}
	
	private Element createImpression() {
		Element impressionTag = doc.createElement("Impression");
		impressionTag.setAttribute("id", "PM");
		impressionTag.setTextContent("http://www.apple.com");		
		
		return impressionTag;
	}

	private void addBeaconsToWrapper(Node item) {
		// TODO Auto-generated method stub
		
	}
}
