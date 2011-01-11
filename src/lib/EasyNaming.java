/**
 *
 * certainesp parties sont extraites de
 * Java Programming with CORBA
 *  Second edition
 *  Andreas Vogel & Keigh Duddy
 *  Wiley
 *
 *  http://www.wiley.com/compbookds/vogel
 *
 * Modification effectu�es par Philippe Lamarre
 * E-mail:
 * Philippe.Lamarre@irin.univ-nantes.fr - Sylvie.Cazalens@irin.univ-nantes.fr
 *
 *
 * Modifications le 10 12 2001 par Nicolas Lemoullec et Vincent Tricoire
 *
 **/


package lib;


// ajout� pour la m�thode servicesDispo
import org.omg.CORBA.*;

// import initiaux
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import java.util.*;


public class EasyNaming  {
    
    private NamingContext root_context;

    public static final int _viaORB = 1;
    public static final int _viaIOR = 2;
    public static final int _viaMachineName = 3;

    private int typeInit;
    private String machineName;;
    private String machinePort;
    private String IOR;

    public String toString() {
		return machineName+":"+machinePort;
    }
    
    public int initType () {
		return typeInit;
    }

    public String machineName() {
		return machineName;
    }

    public String machinePort() {
		return machinePort;
    }

    public String IOR() {
		return IOR;
    }

    /**
     * EasyNaming
     *
     * utilitaire pour le service de nommage Dans cette version, on
     * commence par essayer de regarder quels sont les services
     * disponibles et en particulier s'il y a un service de nommage
     * @param orb ORB utilis�
     *
     **/
    public EasyNaming( org.omg.CORBA.ORB orb) {
        // initialise Naming Service via ORB
		this.typeInit = _viaORB;
        try {
            org.omg.CORBA.Object obj = orb.resolve_initial_references(
                "NameService");
            root_context = NamingContextHelper.narrow( obj );
            
            IOR = orb.object_to_string(root_context);
            
            if( root_context == null ) {
                System.err.println("Returned IOR is not a Naming Context");
            }
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName inex ) {
            System.err.println( inex );
        }
        catch(org.omg.CORBA.SystemException corba_exception) {
            System.err.println(corba_exception);
        }
    }

    /**
     * EasyNaming
     * 
     * Permet de recuperer un service de nommage deja lance, via son ior.
     * 
     */
    public EasyNaming(org.omg.CORBA.ORB orb, String ior){
    	// initialise Naming Service via IOR
    	this.typeInit = _viaIOR;
    	
    	try {
			org.omg.CORBA.Object obj = orb.string_to_object(ior);
			root_context = NamingContextHelper.narrow(obj);
	        if( root_context == null ) {
	            System.err.println("Returned IOR is not a Naming Context");
	        }
    	}
    	catch(org.omg.CORBA.SystemException corba_exception) {
    		System.err.println(corba_exception);
    	}    	

    }
    
    /**
     *
     * EasyNaming
     *
     * Utilitaire pour le service de nommage
     *
     * @param orb ORB utilis�
     * @param nameServiceMachine nom de la machine supportant le service de nom principal utilis� par le serveur
     *
     **/
    public EasyNaming( org.omg.CORBA.ORB orb, String nameServiceMachine, String nameServicePort) {
		this.typeInit = _viaMachineName;
		this.machineName = nameServiceMachine;
		this.machinePort = nameServicePort;
        // initialise Naming Service via le nom de la machine et le port
        try {
		    String chaine = "corbaloc:iiop:"+nameServiceMachine+":"+nameServicePort+"/NameService";
	    	org.omg.CORBA.Object obj = (org.omg.CORBA.Object)orb.string_to_object(chaine);
            root_context = NamingContextHelper.narrow( obj );
            
            IOR = orb.object_to_string(root_context);
            
	        if( root_context == null ) {
                System.err.println("Returned IOR is not a Naming Context");
            }
        }
        catch(org.omg.CORBA.SystemException corba_exception) {
	    	root_context = null;
            System.err.println(corba_exception);
        }
    }


    /**
     *
     * bind_from_string
     *
     * d�claration d'un objet � partir de son nom. Le nom � la forme unix standard (/toto/titi/nom) o� toto et titi sont des domaines et nom et le nom de l'objet
     * si les domaines n'existent pas ils sont cr��s les uns apr�s les autres.
     * S'il y a un probl�me, une exception est lev�e.
     *
     * @param str chaine repr�sentant le nom de l'objet
     * @param obj objet � d�clarer
     **/
    public void bind_from_string( String str, org.omg.CORBA.Object obj )
        throws InvalidName, AlreadyBound, CannotProceed,NotFound, org.omg.CORBA.SystemException, InvalidRootContextException {
		if (root_context != null) {
	    	NameComponent[] name = str2name( str );
	    	NamingContext context = root_context;
	    	NameComponent[] _name = new NameComponent[1];
		    try {
				root_context.bind( name, obj );
	    	}
	    	catch( NotFound not_found ) {
	    		// bind step by step
				// create and bind all non-existent contexts in the path
				for( int i = 0; i < name.length - 1;  i++ ) {
		    		_name[0] = name[i];
		    		try {
						// see if the context exists
						context = NamingContextHelper.narrow(
							context.resolve( _name ) );
			    	} catch( NotFound nf ) {
						// if not then create a new context
						context = context.bind_new_context( _name );
			    	}
			    	// let other exceptions propagate to caller
				}
				// bind last component to the obj argument
				_name[0] = name[ name.length - 1 ];
				context.bind( _name, obj );
	    		}
	    	// let other exceptions propagate to caller
			} else {
	    	// le contexte n'est pas bon, il n'est pas possible de proc�der
	    	throw new InvalidRootContextException();
		}
    }

    /**
     * rebind_from_string
     *
     * red�clarer un serveur qui a d�j� �t� d�clar�. Remplacer l'ancien. La forme du nom est identique � celle utilis�e dans bind_from_string.
     *
     * @param str chaine repr�sentant le nom de l'objet
     * @param obj objet � d�clarer
     *NameService
     **/
    public void rebind_from_string(String str, org.omg.CORBA.Object obj) throws InvalidName, AlreadyBound, CannotProceed, NotFound, org.omg.CORBA.SystemException, InvalidRootContextException {
		if (root_context != null) {
	    	NameComponent[] name = str2name( str );
	    	NamingContext context = root_context;
	    	NameComponent[] _name = new NameComponent[1];
	    	try {
				root_context.rebind( name, obj );
	    	} catch( NotFound not_found ) {
				// bind step by step
				// create and bind all non-existent contexts in the path
				for( int i = 0; i < name.length - 1;  i++ ) {
		    		_name[0] = name[i];
		    		try {
						// see if the context exists
						context = NamingContextHelper.narrow(
							context.resolve( _name ) );
		    		} catch( NotFound nf ) {
						// if not then create a new context
						context = context.bind_new_context( _name );
		    		}
		    		// let other exceptions propagate to caller
				}
				// bind last component to the obj argument
				_name[0] = name[ name.length - 1 ];
				context.bind( _name, obj );
	    	}
	    	// let other exceptions propagate to caller
		} else {
	    	throw new InvalidRootContextException();
		}
    }

    /**
     * unbind_from_string
     *
     * supprimer une d�claration � partir d'un nom repr�sent� par une chaine au format unix standard
     *
     * @param str chaine repr�sentant le nom de l'objet � retirer
     *
     **/
    public void unbind_from_string( String str ) throws InvalidName, NotFound, CannotProceed, org.omg.CORBA.SystemException, InvalidRootContextException {
		if (root_context != null) {
	    	root_context.unbind( str2name( str ) );
		} else {
	    	throw new InvalidRootContextException();
		}
    }

    /**
     * resolve_from_string
     *
     * r�soudre un nom � partir d'un nom donn� sous forme de chaine au format standard unix
     *
     * @param str chaine repr�sentant le nom de l'objet dont on cherche l'IOR.
     *
     **/
    public org.omg.CORBA.Object resolve_from_string( String str ) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		if (root_context != null) {
	    	return root_context.resolve( str2name( str ) );
		} else {
	    	throw new InvalidRootContextException();
		}
    }

    /**
     * get_root_context
     *
     * r�cup�rer le contexte racine pour le service de nommage
     *
     * @return le contexte de nommage racine
     *
     **/
    public NamingContext get_root_context() {
        return root_context;
    }

    /**
     * lister seulement les objets dans un contexte (en mettant un pr�fixe)
     **/
    public String[] list_objects_from_string(String nameContext, String prefix, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		return list_from_string(nameContext, prefix, number, "nobject");
    }
			     
    /**
     * lister seulement les contextes dans un contexte (en mettant un pr�fixe)
     **/
    public String[] list_contexts_from_string(String nameContext, String prefix, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		return list_from_string(nameContext, prefix, number, "ncontext");
    }

    /**
     * lister tous les �l�ments d'un contexte  (en mettant un pr�fixe)
     **/
    public String[] list_from_string(String nameContext, String prefix, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		return list_from_string(nameContext, prefix, number, "all");
    }

    /**
     * obtenir la liste des noms de certains �l�ments dans un contexte particulier en mettant un pr�fixe
     **/
    public String[] list_from_string(String nameContext, String prefix, int number, String concerned) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		if (root_context != null) {
	    	HashSet toReturn = new HashSet();
	    	org.omg.CORBA.Object contextObject = resolve_from_string(nameContext);
	    	NamingContext context = NamingContextHelper.narrow(contextObject);
	    	if (context != null) {
				BindingListHolder bindingList = new BindingListHolder();
				BindingIteratorHolder bindingIterator = new BindingIteratorHolder();
				context.list(number, bindingList, bindingIterator);
				for (int i = 0 ; i < bindingList.value.length ; i++) {
		    		if ((concerned.equals("all")) || ((concerned.equals("nobject")) && (bindingList.value[i].binding_type.value() == BindingType._nobject)) || ((concerned.equals("ncontext")) && (bindingList.value[i].binding_type.value()  == BindingType._ncontext))) {
						toReturn.add(prefix + name2string(bindingList.value[i]));
		    		}
				}
	    	} else {
				return null;
	    	}
	    	String[] toRet = new String[toReturn.size()];
	    	Iterator iter = toReturn.iterator();
	    	int i = 0;
	    	while (iter.hasNext()) {
				toRet[i] = (String) iter.next();
				i++;
	    	}
 	    	return toRet;
		} else {
	    	throw new InvalidRootContextException();
		}
    }

    /**
     * obtenir la liste des noms (objets et contextes) dans un contexte particulier
     **/
    public String[] list_from_string(String nameContext, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
	    return list_from_string(nameContext, "", number);
	}
    
    /**
     * liste de tous les objets et des contextes en chemin absolu (ajout du pr�fixe)
     **/
    public String[] absolute_list_from_string(String nameContext, String prefix, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		return absolute_list_from_string(nameContext, prefix, number, "all");
    }
    
    /**
     * liste des objets en chemin absolu (ajout du prefixe)
     **/
    public String[] absolute_list_of_objects_from_string(String nameContext, String prefix, int number) throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		return absolute_list_from_string(nameContext, prefix, number, "nobject");
    }
   
    /**
     * obtenir la liste des noms dans un contexte particulier sous forme de chemin absolu
     **/
    public String[] absolute_list_from_string(String nameContext, String prefix, int number, String concerned)
        throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
		String finalPrefix = prefix + nameContext;
	    if (nameContext.charAt(nameContext.length() - 1) != '/') {
			finalPrefix = finalPrefix + "/";
		}
	    return list_from_string(nameContext, finalPrefix, number, concerned);
	}

    /**
     * obtenir la liste des noms dans un contexte particulier sous forme de chemin absolu
     **/
    public String[] absolute_list_from_string(String nameContext, int number)
        throws InvalidName, NotFound, CannotProceed, InvalidName, org.omg.CORBA.SystemException, InvalidRootContextException {
	    return absolute_list_from_string(nameContext, "", number);
	}

    /**
     * transformer un binding en string
     *
     * par convention, s'il s'aggit d'un constexte il se termine par /
     * dans la string, ce qui n'est pas le cas pour un objet.
     **/
    public static String name2string(Binding binding) {
		NameComponent[] nameComponents = binding.binding_name;
		String name = "";
		for (int i = 0 ; i < nameComponents.length ; i++) {
	    	if (i > 0) {
				name = name + "/";
	    	}
	    	name = name + nameComponents[i].id;
		}
		if (binding.binding_type.value() == BindingType._ncontext) {
	    	name = name + "/";
		}
		return name;
    }


    /**
     * transformer une string en composant de noms
     **/
    public static NameComponent[] str2name( String str ) throws InvalidName {
		int i, j, c, components, name_length;
		NameComponent[] name;
		char[] char_str = str.toCharArray();
        // count name components (separated by '/')
        for( i = 0, components = 0; i < char_str.length; i++ ) {
	    	if( char_str[i] == '/' ) components++;
        }
        name = new NameComponent[components];
        char[][] id = new char[components][];
        String s[] = new String[components];
        if( char_str[0] != '/' ) {
            throw new InvalidName();
        }
        for( i = 0, c = 0; i < char_str.length; c++ ) {
           // skip separator
           if( char_str[i] == '/' )
               i++;
	           // get length of a name component
    	       for( j = i, name_length = 0; (j < char_str.length ) && ( char_str[j] != '/' ); j++ ) {
            	    name_length++;
           		}
           		id[c] = new char[ name_length ];
           		for( j = 0; j < name_length; j++ ) {
            		id[c][j] = char_str[i+j];
           		}
           		i = j + i + 1;
           		// create name component
           		s[c] = new String( id[c] );
           		name[c] = new NameComponent( s[c], "" );
        	}
        	return name;
    	}

    /**
     *
     * servicesDispo
     *
     * Affichage des services disponibles sur l'ORB
     *
     * @param orb orb utilis�
     *
     **/
    public static void servicesDispo(ORB orb) {
	System.out.println("Services disponibles : ");
	String[] services = orb .list_initial_services();
	if (services.length == 0) {
	    System.out.println("Pas de service disponible");
	} else {
	    for (int i = 0 ; i < services.length; i++) {
		System.out.println("   " + i + " --> " + services[i]);
	    }; // for
	}; //if
    }; // servicesDispo
    

    /**
     * partie sp�cifique au projet DESS 2001-2002 Service d'ench�res
     *
     * convention : le nom d'un objet est sous la forme
     * <nomMachine>:<nomPort>:<CheminObjet>
     * <nomMachine>:<nomPort>:<CheminObjet>
     **/
    public static org.omg.CORBA.Object resoudreReferenceObjet(org.omg.CORBA.ORB orb, String nomObjet) {
	// recherche de l'indice des deux points s�parant le nom de la machine du port
	int indiceDeuxPoints = nomObjet.indexOf(':');
	// recherche du slash correspondant au d�but du nom
	int indiceSlash = nomObjet.indexOf(':', indiceDeuxPoints+1);
	// positionner les variables correctement
	if (indiceSlash >= 1) {
	    String nomMachine = nomObjet.substring(0, indiceDeuxPoints);
	    String nomPort    = nomObjet.substring(indiceDeuxPoints+1, indiceSlash);
	    String cheminObjet  = nomObjet.substring(indiceSlash+1, nomObjet.length());
	    EasyNaming nommage = new EasyNaming(orb, nomMachine, nomPort);
	    try {
		return nommage.resolve_from_string(cheminObjet);
	    } catch (Exception e) {
		return null;
	    	}
	} else {
	    System.err.println("[ERROR] le nom " + nomObjet + " n'est pas correct");
	    return null;
	}
    }
    

    /**
     * convention : le nom d'un objet est sous la forme
     * <nomMachine>:<nomPort>:<CheminObjet>
     * bind un objet a partir de son nom
     *
     **/
    public static void inscriptionObjet(org.omg.CORBA.ORB orb, String nomObjet, org.omg.CORBA.Object obj) {
	// recherche de l'indice des deux points s�parant le nom de la machine du port
	int indiceDeuxPoints = nomObjet.indexOf(':');
	// recherche du slash correspondant au d�but du nom
	int indiceSlash =  nomObjet.indexOf(':', indiceDeuxPoints+1);
	// positionner les variables correctement
	if (indiceSlash >= 1) {
	    String nomMachine =  nomObjet.substring(0, indiceDeuxPoints);
	    String nomPort    =  nomObjet.substring(indiceDeuxPoints+1, indiceSlash);
	    String cheminObjet  =  nomObjet.substring(indiceSlash+1,  nomObjet.length());
	    EasyNaming nommage = new EasyNaming(orb, nomMachine, nomPort);
	    try {
		nommage.bind_from_string(cheminObjet,obj);
	    } catch (Exception e) {
	    }
	} else {
	    System.err.println("[ERROR] le nom " +  nomObjet + " n'est pas correct");
	}
    }


    /**
     * convention : le nom d'un objet est sous la forme
     * <nomMachine>:<nomPort>:<CheminObjet>
     * unbind un objet a partir de son nom
     *
     **/
    public static void desinscriptionObjet(org.omg.CORBA.ORB orb, String nomObjet) {
	// recherche de l'indice des deux points s�parant le nom de la machine du port
	int indiceDeuxPoints = nomObjet.indexOf(':');
	// recherche du slash correspondant au d�but du nom
	int indiceSlash =  nomObjet.indexOf(':', indiceDeuxPoints+1);
	// positionner les variables correctement
	if (indiceSlash >= 1) {
	    String nomMachine =  nomObjet.substring(0, indiceDeuxPoints);
	    String nomPort    =  nomObjet.substring(indiceDeuxPoints+1, indiceSlash);
	    String cheminObjet  =  nomObjet.substring(indiceSlash+1,  nomObjet.length());
	    EasyNaming nommage = new EasyNaming(orb, nomMachine, nomPort);
	    try {
		nommage.unbind_from_string(cheminObjet);
	    } catch (Exception e) {
	    }
	} else {
	    // ce n'est pas nom correct pour un agent bonom...
	    System.err.println("[ERROR] le nom " +  nomObjet + " n'est pas correct");
	}
    }
	
}
