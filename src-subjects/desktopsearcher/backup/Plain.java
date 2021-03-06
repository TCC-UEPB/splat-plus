
import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;



/**
 * Indexiert Textdateien.
 * 
 * Der einfachste ContentHandler: Er liest nur den Dateiinhalt
 * ein und verwendet den Dateinamen ohne Endung als Dateititel.
 * 
 * @author Mr. Pink
 */
public class Plain extends ContentHandler {
	
	/**
	 * Ermittelt die von diesem CH gesetzten indexierten Felder
	 * 
	 * @return  die Namen der Felder
	 */
	public String[] getIndexedFields() {
		return new String[] {"content", "title"};
	}
	
	/**
	 * Fragt ab, ob die Klasse fuer die uebergebene Datei zustaendig ist.
	 * 
	 * @param  filename  der Dateiname ohne Pfad
	 * @return           true, falls ja, sonst false
	 */
	public boolean handles(String filename) {
		return filename.endsWith(".txt");
	}
	
	/**
	 * Wurde das Dokument mit diesem ContentHandler erstellt?
	 * 
	 * @param  doc  das Dokument
	 * @return      true, falls ja (type = plain), sonst false
	 */
	public static boolean isOwnerOfDocument(Document doc) {
		return doc.getField("type").stringValue().equals("plain");
	}
	
	/**
	 * Indexieren einer Textdatei.
	 * 
	 * @param filename  Pfad der zu indexierenden Datei
	 * @param writer    der zu verwendende IndexWriter
	 * @return          true wenn die Datei fehlerfrei indexiert werden konnte, sonst false
	 */
	public boolean index(String filename, IndexWriter writer) {
		try {
			String plainText = Plain.getFileContents(filename).trim();

			// Wir fuegen dem Dokument die von uns zur Verfuegung
			// gestellten Felder hinzu.

			Document doc = new Document();
			
			Field field = new Field("content", plainText + " ", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);

			doc.add(field);
			
			doc.add(new Field("type", "plain", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.YES));
			
			// Dateititel = Dateiname
			
			File   f         = new File(filename);
			String basename  = f.getName();
			int    lastIndex = basename.lastIndexOf('.');
			
			if (lastIndex != 0) {
				String title = basename.substring(0, lastIndex);
				doc.add(new Field("title", title + " ", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			}
			

			ContentHandler.addDefaultFields(doc, filename);

			// Dokument dem Index hinzufuegen

			writer.addDocument(doc);

			return true;
		} catch (Exception e) {
			System.err.println(e);
			return false;
		}
	}
}