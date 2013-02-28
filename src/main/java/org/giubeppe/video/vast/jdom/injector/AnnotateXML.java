package org.giubeppe.video.vast.jdom.injector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class AnnotateXML {

	private static final String IMP_BEACON = "http://www.apple.com";

	Document doc = null;

	static Logger logger = Logger.getAnonymousLogger();

	public static void main(String[] args) throws Exception {
		AnnotateXML annotator = new AnnotateXML();
		annotator.parse();
		annotator.annotate();
		annotator.dump();
	}

	public void dump() throws IOException {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter("/tmp/output.xml"));
	}

	public void annotate() {
		Element vastEl = doc.getRootElement();
		if (vastEl != null) {
			List<Element> ads = vastEl.getChildren("Ad");
			for (Element ad : ads) {
				Element wrapperEl = ad.getChild("Wrapper");
				if (wrapperEl != null) {
					annotateWrapper(wrapperEl);
				} else {
					Element inlineEl = ad.getChild("InLine");
					if (inlineEl != null) {
						annotateInline(inlineEl);
					}
				}
			}
		}
	}

	private void annotateInline(Element inlineEl) {
		annotate(inlineEl);
	}

	private void annotateWrapper(Element wrapperEl) {
		annotate(wrapperEl);
	}

	private void annotate(Element genericEl) {

		List<Element> inlineChildren = genericEl.getChildren();
		int idx = 0;
		int impIdx = -1;
		int crtIdx = -1;
		for (Element el : inlineChildren) {
			if (el.getName().equals("Impression")) {
				impIdx = idx;
				break;
			}
			if (el.getName().equals("Creatives")) {
				crtIdx = idx;
				annotateCreative(el);
			}
			idx++;
		}

		/*
		 * if we have impression, we'll prepend, otherwise, we'll just put it
		 * before the creatives
		 */
		if (impIdx >= 0) {
			inlineChildren.add(impIdx, getImpBeacon(IMP_BEACON));
		} else {
			if (crtIdx >= 0) {
				inlineChildren.add(crtIdx, getImpBeacon(IMP_BEACON));
			}
		}
	}

	private void annotateCreative(Element el) {
		Element creativeEl = el.getChild("Creative");
		Element linearEl = creativeEl.getChild("Linear");
		if (linearEl != null) {
			Element trkEvents = linearEl.getChild("TrackingEvents");
			// TODO
		} else {		
			Element companionEl = creativeEl.getChild("CompanionAds");
			if (companionEl != null) {
				// TODO
			} // else look for non linear
		}
	}

	public void parse() throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File("liverail.xml");

		doc = (Document) builder.build(xmlFile);
	}

	private Element getImpBeacon(String impBeacon) {
		Element beaconElement = new Element("Impression").setAttribute("id", "PM-beacon")
				.setText(impBeacon);
		return beaconElement;
	}

	private Element getTrackerBeacon(String eventName, String eventBeacon) {
		Element beaconElement = new Element("Tracking").setAttribute("event", eventName)
				.setText(eventBeacon);
		return beaconElement;
	}
}
