package com.ipiecoles.java.java230;


import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) {
       String fileName = "employes.csv";
       try {
		readFile(fileName);
       } catch (IOException e) {
    	System.out.println("Le nom du fichier n'est pas valide" + e.getMessage());
       }
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     * @throws IOException 
     */
    public List<Employe> readFile(String fileName) throws IOException{
        Stream<String> stream;
        stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        //TODO
        Integer i = 0;
        
        for(String ligne : stream.collect(Collectors.toList())) {
            i++;
            try{
                processLine(ligne);
            }catch (BatchException e){
                System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);

            }
        }

        return employes;
    }


    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
    	
//Check that the first char of the String is M, T or C    	
    	if(!ligne.matches("^[MTC]{1}.*")) {
            throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
        }
    	
//Splitting the String to get an array of string we can work with
    	String[] tableau = ligne.split(",");
    	
//Checking that the matricule has the right format
    	if(!tableau[0].matches(REGEX_MATRICULE)) {
    		throw new BatchException("La chaine "+tableau[0] +" ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");
    	}
    	
//Check NB de champs for each entity    	
    	if(tableau[0].matches("^[M]{1}.*") && tableau.length != NB_CHAMPS_MANAGER) {
    		throw new BatchException("La ligne manager ne contient pas 5 éléments mais " + tableau.length);
    	}
    	if(tableau[0].matches("^[C]{1}.*") && tableau.length != NB_CHAMPS_COMMERCIAL) {
    		throw new BatchException("La ligne commercial ne contient pas 7 éléments mais " + tableau.length);
    	}
    	if(tableau[0].matches("^[T]{1}.*") && tableau.length != NB_CHAMPS_TECHNICIEN) {
    		throw new BatchException("La ligne technicien ne contient pas 7 éléments mais " + tableau.length);
    	}
    	
//Check salary is valid
    	try {
            Double.parseDouble(tableau[4]);
        } catch (Exception e) {
            throw new BatchException(tableau[4] + " n'est pas un nombre valide pour un salaire");
        }

//Check the date format is valid
    	try {
    		DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tableau[3]); 
    	} catch(Exception e) {
    		throw new BatchException(tableau[3] + " ne respecte pas le format de date dd/MM/yyyy");
    	}

//Check le chiffre d'affaire du commercial
    	if(tableau[0].matches("^[C]{1}.*")){
    		try {
        		Double.parseDouble(tableau[5]);
        	} catch(Exception e) {
        		throw new BatchException("Le chiffre d'affaire du commercial est incorrect");
        	}
    	}
//Check la performance du commercial    	
    	if(tableau[0].matches("^[C]{1}.*")){
    		try {
        		Double.parseDouble(tableau[6]);
        	} catch(Exception e) {
        		throw new BatchException("La performance du commercial est incorrecte");
        	}
    	}

//Check le format du grade du technicien    	
    	if(tableau[0].matches("^[T]{1}.*")) {
    		try {
    			Double.parseDouble(tableau[5]);
    		} catch(Exception e) {
    			throw new BatchException("Le grade du technicien est incorrect : " + tableau[5]);
    		}
    	}
    	
    	if(tableau[0].matches("^[T]{1}.*")) {
    		Integer myInt = new Integer(tableau[5]);
    		if(myInt < 1 || myInt > 5) {
    			throw new BatchException("Le grade doit être compris entre 1 et 5 : " + tableau[5]);
    		}
    	}

//Check le format du manager du technicien
    	if(tableau[0].matches("^[T]{1}.*") && !tableau[6].matches(REGEX_MATRICULE_MANAGER)) {
    		throw new BatchException("La chaîne " + tableau[6] + " ne respecte pas l'expression régulière ^M[0-9]{5}$ ");
    	}

//Check que le manager du technicien existe en base
    	if(tableau[0].matches("^[T]{1}.*") && tableau[6].matches(REGEX_MATRICULE_MANAGER)) {
    		Employe emp = employeRepository.findByMatricule(tableau[6]);
    		if (emp == null) {
    			throw new BatchException("Le manager de matricule " +tableau[6]+ " n'a pas été trouvé dans en base de données");
    		}    		
    	}    	
    }

 
    	

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
    	
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
    }

}
