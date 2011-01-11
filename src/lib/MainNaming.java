package lib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.omg.CORBA.ORB;

public class MainNaming {

    /**
     * @param args
     */
    public static void main(String[] args) {
                            
    Properties props = new Properties();
    props.put("org.omg.CORBA.ORBInitialPort", "1050");
    props.put("org.omg.CORBA.ORBInitialHost", "localhost");
    

            ORB orb = ORB.init(args, props);
            EasyNaming en = new EasyNaming(orb);
            
            // On recupere l'ior pour le retourner aux utilisateurs
            PrintWriter out;
            try {
                    out = new PrintWriter(new FileOutputStream("iorNammingService.ref"));
                    out.println(orb.object_to_string(en.get_root_context()));
                out.close();
            } catch (FileNotFoundException e1) {
                    System.out.println("Probleme de creation du fichier IOR");
                    e1.printStackTrace();
            }
                    
            System.out.println("Namming Service activé !!");
            System.out.println(en.IOR());
            
            System.out.println();           
            System.out.println("Appuyer sur [ENTRER] pour terminer");
            
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            try {
                    String sentence = inFromUser.readLine();
            } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            System.out.println("Au revoir!");
            
    }

}
