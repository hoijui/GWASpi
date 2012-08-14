/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.gui;

import org.gwaspi.gui.utils.URLInDefaultBrowser;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class GetEnsemblRegionHTML extends JFrame {

	public static void main(String[] args) throws MalformedURLException, IOException {

//            System.setProperty("proxySet", "true");
//            System.setProperty("http.proxyHost", "proxy.upf.edu");
//            System.setProperty("http.proxyPort", "8080");

            System.setProperty("java.net.useSystemProxies", "true");

            final String hostName="http://www.ensembl.org";
            //final URL u =  new URL(hostName+"/Homo_sapiens/Component/Location/Web/Region?_rmd=18fa;r=1:50000-150000");
            final URL u =  new URL(hostName+"/Homo_sapiens/Location/Overview?r=1:50000-150000");

            InputStream inputStr = u.openStream();
            DataInputStream br = new DataInputStream(new BufferedInputStream(inputStr));
            
            String s;
            //String webContent = "<html>";
            String webContent = "";
            while ((s = br.readLine()) != null) {


                if (s.contains("<div class=\"content\">")) {
                    if (s.contains("class=\"ajax_load\"")) {
                        webContent += s.replace("value=\"/Homo_sapiens/", "value=\"" + hostName+"/Homo_sapiens/");
                    }
                }
//                if(s.contains("<img")){
//                    s = s.substring(s.indexOf("<img src=\""), s.indexOf("/>",s.indexOf("<img src=\""))+2);
//                    webContent += s.replace("<img src=\"", "<img src=\""+hostName);
//                }
//                else if(s.contains("<map")){
//                    webContent += s;
//                } else if(s.contains("</map") ){
//                    webContent += s;
//                } else if(s.contains("<area")){
//                    webContent += s;
//                }

                else {
                    if(s.startsWith("<")){
                        s = s.replaceFirst(" ", " style=\"visibility:hidden;\" ");
                        webContent += s;
                    }
                    
                }

                //webContent += s;

                
            }
//            webContent += "<map name=\"mainmap\">";
//            webContent += "<area shape=\"rect\" coords=\"0,0,800,337\" href=\"http://tata\">";
//            webContent += "</map>";

            //webContent += "</html>";

            JFrame f = new JFrame("HTML Example");

            JEditorPane jep = new JEditorPane();
            jep.setEditable(false);
            jep.setEditorKit(new HTMLEditorKit());
            jep.addHyperlinkListener(new HyperlinkListener(){
                @Override
                public void hyperlinkUpdate(HyperlinkEvent ev){
                    if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            //System.out.println("URL: " + ev.getURL() + " desc: " + ev.getDescription() + " element: " + ev.getSourceElement());
                            URLInDefaultBrowser.browseGenericURL("http://www.ensembl.org/Homo_sapiens/Location/Overview?"+u.getQuery());
                        } catch(IOException ioe) {
                            // Some warning to user
                        }
                    }
                }


            });
//            jep.addHyperlinkListener(new HyperlinkListener() {
//                @Override
//                public void hyperlinkUpdate(HyperlinkEvent e) {
//                    try {
//                        URLInDefaultBrowser.browseGenericURL(e.getURL().getPath());
//                    } catch (IOException ex) {
//                        Logger.getLogger(GetEnsemblRegionHTML.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });
            jep.setText(webContent);


            f.add(new JScrollPane(jep), "Center");
            //f.add(jep);
            f.setSize(830, 387);

            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            f.setVisible(true);
	}

}