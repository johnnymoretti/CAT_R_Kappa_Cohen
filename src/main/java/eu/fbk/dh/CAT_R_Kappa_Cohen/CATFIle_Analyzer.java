package eu.fbk.dh.CAT_R_Kappa_Cohen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by giovannimoretti on 12/02/17.
 */
public class CATFIle_Analyzer implements Comparable{

    private File file;
    private String classe;
    private String cat_file_name;
    private ConcurrentHashMap<Integer,AtomicInteger> subjects = new ConcurrentHashMap<>();
    private boolean annotated = false;




    public CATFIle_Analyzer(File file, String classe){
       this.file = file;
       this.classe = classe;
    }

    public void analize(){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            InputStream stream = new FileInputStream(this.file);


            Document doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();




            XPathExpression expr;
            NodeList nl;

            expr = xpath.compile("/Document");
            Node doc_node = (Node) expr.evaluate(doc, XPathConstants.NODE);

            this.cat_file_name  = ((Element) doc_node).getAttribute("doc_name");


            expr = xpath.compile("/Document/token");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);


            for (int i = 0; i < nl.getLength(); i++) {
                Element t_elem = (Element) nl.item(i);

                Integer s = Integer.parseInt(t_elem.getAttribute("t_id"));
                this.subjects.putIfAbsent(s,new AtomicInteger(0));
            }


            expr = xpath.compile("/Document/Markables/"+this.classe+"/token_anchor");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);


            for (int i = 0; i < nl.getLength(); i++) {
                Element token_anchor = (Element) nl.item(i);
                subjects.get(Integer.parseInt(token_anchor.getAttribute("t_id"))).addAndGet(1);
                this.annotated = true;

            }


            //System.out.println(subjects.toString());



        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String getCat_file_name() {
        return cat_file_name;
    }

    public ConcurrentHashMap<Integer, AtomicInteger> getSubjects() {
        return subjects;
    }

    @Override
    public int compareTo(Object o) {
        CATFIle_Analyzer other = (CATFIle_Analyzer) o;
        if (this.cat_file_name.compareTo( other.cat_file_name) == 0 && (this.subjects.size() == other.getSubjects().size())){
            return 0;
        }else{
            return -1;
        }

    }

    public boolean isAnnotated() {
        return annotated;
    }
}
