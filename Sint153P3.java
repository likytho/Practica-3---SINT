/*
 * Práctica 3 SINT
 *
 * Pedro Tubío Figueira
 * SINT 153
 *
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;



public class Sint153P3 extends HttpServlet {


    MongoClient mongoClient;    

    ArrayList<Document> listaDocumentosJSON = new ArrayList<Document>();
    ArrayList<String> listaErrores = new ArrayList<String>();

    boolean executed = false;

    String fase1 = "";

    String fase2 = "";
    String fase2Autor = "";
    Map<Integer, String> mapFase2 = new TreeMap<Integer, String>();
    Map<String, String> mapFase2Consulta1 = new TreeMap<String, String>();
    Map<String, String> mapFase2Consulta1ID = new TreeMap<String, String>();

    String fase3 = "";
    String autorFase3 = "";
    Map<Integer, String> mapFase3 = new TreeMap<Integer, String>();
    Map<Integer, ArrayList<String>> mapFase3Consulta1Aux = new TreeMap<Integer, ArrayList<String>>();

    String fase4 = "";
    Map<String, Integer> mapFase4 = new TreeMap<String, Integer>();


    //MÉTODO DE INICIALIZACIÓN
    public void init(ServletOutputStream salida) throws ServletException, IOException {
         mongoClient = new MongoClient ("127.0.0.1", 27017);
    }

    public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    //MÉTODO PRINCIPAL
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {


        //Salida será el printer de nuestras webs para las consultas
        ServletOutputStream salida = res.getOutputStream();
        res.setContentType("text/html");

        salida.println("<!DOCTYPE html>");
        salida.println("<head>");
        salida.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-15\">");
        salida.println("<title>Servicio IML</title>");
        salida.println("<link href=\"p2/iml.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\">");
        salida.println("</head>");
        salida.println("<body background=\"p2/notas.jpg\">");
        salida.println("<div id=\"wrapper\">");
        salida.println("<div id=\"container\">");


        //Todo este tocho está dedicado a las fases
        if ((req.getParameter("fase") == null) || (req.getParameter("fase").equals("0"))) {

            init(salida);

            if (!executed){

                MongoDatabase dbase = mongoClient.getDatabase("dbsint");
                MongoCollection<Document> collJSON = dbase.getCollection("IML");
                List<Document> foundDocument = collJSON.find().into(new ArrayList<Document>());
                parseoJSONDB(foundDocument);
            }

            executed = true;

            fase1 = "";            fase2 = "";  fase2Autor = "";           fase3 = "";            fase4 = "";        autorFase3 = "";
            mapFase2.clear();            mapFase2Consulta1.clear();            mapFase2Consulta1ID.clear();
            mapFase3.clear();            mapFase3Consulta1Aux.clear();
            mapFase4.clear();


            salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
            salida.println("<h2>Por favor, realice una selección:</h2>");
            salida.println("<form method=GET action='?fase=1'>");
            salida.println("<input type='radio' name='consulta' value='1' checked> Lista de canciones de un álbum.<br>");
            salida.println("<input type='radio' name='consulta' value='2'> Número de canciones de un estilo.<br><br>");
            salida.println("<input type='submit' value='Enviar'>");
            salida.println("<input type='hidden' name='fase' value='1'>");
            salida.println("</form>");
            salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
            salida.println("</div></div>");


            salida.println("<div id=\"wrapper\">");
            salida.println("<div id=\"container\">");
            salida.println("<h3>Notificaciones:</h3>");

            if(!listaErrores.isEmpty()){
                for(int u=0; u<listaErrores.size(); u++){
                    salida.println("<font color=\"red\"> " + listaErrores.get(u) + "</font>");
                }
            }

            salida.println("</div></div>");

            salida.println("</body>");
            salida.println("</html>");

        } else {
            //Fase 1 -> Escoger tipo de consulta
            if (req.getParameter("fase").equals("1")) {
                if (req.getParameter("consulta").equals("1")) {
                    fase1 = "Lista de canciones de un álbum";
                    fase2Album(req, res, salida);

                }
                if (req.getParameter("consulta").equals("2")) {
                    fase1 = "Número de canciones de un estilo";
                    fase2Estilo(req, res, salida);
                }
            }

            //Fase 211 -> Consulta sobre la lista de canciones de un álbum + intérprete.
            if (req.getParameter("fase").equals("211")) {
                if (req.getParameter("interprete") == null) {
                    fase2 = "";
                    fase2Album(req, res, salida);
                } else {
                    String fase2Aux = req.getParameter("interprete");
                    String [] fase2AuxAux = fase2Aux.split("#");
                    fase2 = fase2AuxAux[0];
                    fase2Autor = fase2AuxAux[1];
                    fase3Album(req, res, salida);
                }
            }

            //Fase 212 -> + álbum
            if (req.getParameter("fase").equals("212")) {
                if (req.getParameter("album") == null) {
                    fase3 = "";
                    fase3Album(req, res, salida);

                } else {
                    fase3 = req.getParameter("album");
                    faseFinalAlbum(req, res, salida);
                }
            }

            //Fase 221 -> Número de canciones de un estilo + anho
            if (req.getParameter("fase").equals("221")) {
                if (req.getParameter("anho") == null) {
                    fase2 = "";
                    fase2Estilo(req, res, salida);
                } else {
                    fase2 = req.getParameter("anho");
                    fase3Estilo(req, res, salida);
                }
            }

            //Fase 222 -> + album
            if (req.getParameter("fase").equals("222")) {
                if (req.getParameter("album2") == null) {
                    fase3 = "";
                    autorFase3 = "";
                    fase3Estilo(req, res, salida);
                } else {
                    String[] fase3Aux = req.getParameter("album2").split("#");
                    fase3 = fase3Aux[0];
                    autorFase3 = fase3Aux[1];
                    fase4Estilo(req, res, salida);
                }
            }

            //Fase 223 -> + estilo
            if (req.getParameter("fase").equals("223")) {

                if (req.getParameter("estilo") == null) {
                    fase4 = "";
                    fase4Estilo(req, res, salida);
                } else {
                    fase4 = req.getParameter("estilo");
                    faseFinalEstilo(req, res, salida);
                }
            }
        }
    }


    //PRIMERA CONSULTA
    public void fase2Album(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<h2>Fase 1: " + fase1 + "</h2>");
        salida.println("<h2>Por favor, seleccione un intérprete:</h2>");
        salida.println("<form method=GET action='?fase=211'>");

        consultaFase2Album();

        if(!mapFase2Consulta1.isEmpty()){

            Iterator iterador = mapFase2Consulta1.keySet().iterator();
            while(iterador.hasNext()){

                String key = (String) iterador.next();
                String Nombre = mapFase2Consulta1.get(key);
                String ID = mapFase2Consulta1ID.get(Nombre);

                salida.println("<input type='radio' name='interprete' value='" + ID + "#" + Nombre + "' checked> " + Nombre + ".<br>");
            }

            salida.println("<input type='radio' name='interprete' value='Todos#Todos' checked> Todos.<br>");
            salida.println("<input type='submit' value='Enviar'><br>");
            salida.println("<input type='hidden' name='fase' value='211'>");

        } else {
            salida.println("<h2>No hay opciones disponibles, la consulta no puede continuar.</h2>");
        }

        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=0'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }

    public void fase3Album(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<h2>Fase 1: " + fase1 + " || Fase 2: " + fase2Autor + "</h2>");
        salida.println("<h2>Por favor, seleccione un álbum:</h2>");
        salida.println("<form method=GET action='?fase=212'>");

        consultaFase3Album();

        if(!mapFase3Consulta1Aux.isEmpty()){

            Iterator iterador = mapFase3Consulta1Aux.keySet().iterator();
            while(iterador.hasNext()){

                Integer key = (Integer) iterador.next();
                ArrayList <String> arrayAux = mapFase3Consulta1Aux.get(key);


                for (int h=0; h<arrayAux.size(); h++){
                    salida.println("<input type='radio' name='album' value='" + arrayAux.get(h) + "' checked> " + arrayAux.get(h) + " ( " + key + " ).<br>");
                }
            }

            salida.println("<input type='radio' name='album' value='Todos' checked> Todos.<br>");
            salida.println("<input type='submit' value='Enviar'> <br>");

        } else {
            salida.println("<h2>No hay opciones disponibles, la consulta no puede continuar.</h2>");
        }

        salida.println("<input type='hidden' name='fase' value='212'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=211'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }

    public void faseFinalAlbum(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        ArrayList<String> listaCanciones = null;

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<form method=GET action='?fase=41'>");
        salida.println("<h3>Su selección ha sido:</h3>");
        salida.println("<h4>Fase 1: " + fase1 + " || Fase 2: " + fase2 + " || Fase 3: " + fase3 + "</h4>");
        salida.println("<h3>El resultado de su consulta es el siguiente:</h3>");
        salida.println("<h4>");
        salida.println("<ul>");

        listaCanciones = consultaFaseFinalAlbum();

        if(!listaCanciones.isEmpty()){

            for (int a=0; a<listaCanciones.size(); a++){
                salida.println("<li> " + listaCanciones.get(a) + "<br>");
            }
        } else {
            salida.println("<h2>No hay canciones que mostrar.</h2>");
        }

        salida.println("</ul>");
        salida.println("</h4>");
        salida.println("<input type='hidden' name='fase' value='41'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=212'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }


    //SEGUNDA CONSULTA
    public void fase2Estilo(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<h2>Fase 1: " + fase1 + "</h2>");
        salida.println("<h2>Por favor, seleccione un año:</h2>");
        salida.println("<form method=GET action='?fase=221'>");

        consultaFase2Estilo();

        if(!mapFase2.isEmpty()){
            Iterator iterator = mapFase2.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = (Integer) iterator.next();
                salida.println("<input type='radio' name='anho'  value='" + mapFase2.get(key) + "' checked> " + mapFase2.get(key) + ".<br>");
            }

            salida.println("<input type='radio' name='anho' value='Todos' checked> Todos.<br>");
            salida.println("<input type='submit' value='Enviar'> <br>");

        } else {
            salida.println("<h2>No hay opciones disponibles, la consulta no puede continuar.</h2>");
        }

        salida.println("<input type='hidden' name='fase' value='221'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=0'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }

    public void fase3Estilo(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        TreeMap<String, ArrayList<String>> mapAux = null;

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<h2>Fase 1: " + fase1 + " || Fase 2: " + fase2 + "</h2>");
        salida.println("<h2>Por favor, seleccione un álbum:</h2>");
        salida.println("<form method=GET action='?fase=222'>");


        mapAux = consultaFase3Estilo();


        if(!mapAux.isEmpty()){

            Iterator iterador = mapAux.keySet().iterator();
            while(iterador.hasNext()){

                String key = (String) iterador.next();
                ArrayList<String> arrayAux = mapAux.get(key);

                salida.println("<b>Autor: </b>" + key + "<br>");

                for (int a=0; a<arrayAux.size(); a++){
                    String albumAux = arrayAux.get(a);

                    String [] splitted = albumAux.split("#");

                    salida.println("<input type='radio' name='album2' value='"+ albumAux +"' checked> " + splitted[0]  + ".<br>");
                }
                salida.println("<br>");
            }
            salida.println("<input type='radio' name='album2' value='Todos#Todos' checked> Todos.<br>");
            salida.println("<input type='submit' value='Enviar'> <br>");
        } else {
            salida.println("<h2>No hay opciones disponibles, la consulta no puede continuar.</h2>");
        }

        salida.println("<input type='hidden' name='fase' value='222'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=221'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }

    public void fase4Estilo(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<h2>Fase 1: " + fase1 + " || Fase 2: " + fase2 + " || Fase 3: " + fase3 + "</h2>");
        salida.println("<h2>Por favor, seleccione un estilo:</h2>");
        salida.println("<form method=GET action='?fase=223'>");

        consultaFase4Estilo();

        if(!mapFase4.isEmpty()){
            Iterator iterador = mapFase4.keySet().iterator();
            while (iterador.hasNext()){
                String key = (String) iterador.next();
                salida.println("<input type='radio' name='estilo' value='"+key+"' checked> "+key+".<br>");
            }

            salida.println("<input type='radio' name='estilo' value='Todos' checked> Todos.<br>");
            salida.println("<input type='submit' value='Enviar'> <br>");
        } else {
            salida.println("<h2>No hay opciones disponibles, la consulta no puede continuar.</h2>");
        }

        salida.println("<input type='hidden' name='fase' value='223'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=222'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }

    public void faseFinalEstilo(HttpServletRequest req, HttpServletResponse res, ServletOutputStream salida) throws ServletException, IOException {

        int totalCanciones = 0;

        salida.println("<h1>SERVICIO DE CONSULTA DE INFORMACIÓN MUSICAL</h1>");
        salida.println("<form method=GET action='?fase=42'>");
        salida.println("<h3>Su selección ha sido:</h3>");
        salida.println("<h4>Fase 1: " + fase1 + " || Fase 2: " + fase2 + " || Fase 3: " + fase3 + " || Fase 4: " + fase4 + "</h4>");
        salida.println("<h3>El resultado de su consulta es el siguiente:</h3>");
        salida.println("<h4>");

        totalCanciones = consultaFaseFinalEstilo();

        salida.println("El número de canciones es: " + totalCanciones + ".<br>");
        salida.println("</h4>");
        salida.println("<input type='hidden' name='fase' value='42'>");
        salida.println("<input type='submit' value='Inicio' onClick='form.fase.value=0'>");
        salida.println("<input type='submit' value='Atrás' onClick='form.fase.value=223'>");
        salida.println("</form>");
        salida.println("<h5>Servicio de consulta de información musical (sint153 - Pedro Tubío Figueira).</h5>");
        salida.println("</div></div>");
        salida.println("</body>");
        salida.println("</html>");
    }


    //MÉTODOS BÚSQUEDA FASES
    //CONSULTA 1
    public void consultaFase2Album()  {
        mapFase2Consulta1.clear();
        mapFase2Consulta1ID.clear();

        for (int i = 0; i < listaDocumentosJSON.size(); i++) {
            Document docAux = listaDocumentosJSON.get(i);

            String nombre = "";
            String ID = "";

            Document docAuxInterprete = (Document) docAux.get("Interprete");
            Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

            if(docAuxNombre.containsKey("NombreC")) nombre = docAuxNombre.getString("NombreC");
            if(docAuxNombre.containsKey("NombreG")) nombre = docAuxNombre.getString("NombreG");

            ID = docAuxNombre.getString("Id");

            mapFase2Consulta1.put(nombre, nombre);
            mapFase2Consulta1ID.put(nombre, ID);
        }
    }

    public void consultaFase3Album()  {

        mapFase3Consulta1Aux.clear();

        if (fase2.equalsIgnoreCase("Todos")) {

            for (int j = 0; j < listaDocumentosJSON.size(); j++) {

                Document docAux = listaDocumentosJSON.get(j);
                Document docAuxInterprete = (Document) docAux.get("Interprete");

                obtenerAlbums(docAuxInterprete);
            }
        } else {
            for (int j = 0; j < listaDocumentosJSON.size(); j++) {

                Document docAux = listaDocumentosJSON.get(j);
                Document docAuxInterprete = (Document) docAux.get("Interprete");
                Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

                if (docAuxNombre.getString("Id").equals(fase2)) {
                    obtenerAlbums(docAuxInterprete);
                }
            }
        }
    }

    public ArrayList<String> consultaFaseFinalAlbum() throws ServletException {

        ArrayList<String> listadoFinalCanciones = new ArrayList<String>();

        if (fase2.equalsIgnoreCase("Todos")) {

            Document docAux = null;

            for (int m = 0; m < listaDocumentosJSON.size(); m++) {

                docAux = listaDocumentosJSON.get(m);
                Document docAuxInterprete = (Document) docAux.get("Interprete");

                comprobadorFase3ConsultaFinalAlbum(docAuxInterprete, listadoFinalCanciones); //Comprobamos la selección de la fase 3 y actuamos en consecuencia.


            }
        } else {

            Document docAuxInterprete = null;
            Document docAuxNombre = null;

            for (int x=0; x<listaDocumentosJSON.size(); x++){
                Document docAux = listaDocumentosJSON.get(x);
                docAuxInterprete = (Document) docAux.get("Interprete");
                docAuxNombre = (Document) docAuxInterprete.get("Nombre");

                if(docAuxNombre.getString("Id").equalsIgnoreCase(fase2)) break;
            }

            comprobadorFase3ConsultaFinalAlbum(docAuxInterprete, listadoFinalCanciones);
        }
        return listadoFinalCanciones;
    }

    //CONSULTA 2
    public void consultaFase2Estilo(){

        mapFase2.clear();

        for (int i=0; i<listaDocumentosJSON.size(); i++){

            Document docAux = listaDocumentosJSON.get(i);
            Document docAuxInterprete = (Document) docAux.get("Interprete");

            List<Document> listAlbumAux = null;
            Document docAuxAlbum = null;

            boolean ListAlbum = false, docAlbum = false;

            if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                listAlbumAux = (List) docAuxInterprete.get("Album");
                ListAlbum = true;
            }

            if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                docAuxAlbum = (Document) docAuxInterprete.get("Album");
                docAlbum = true;
            }


            if(docAlbum){
                if(!mapFase2.containsValue(docAuxAlbum.getString("Año"))){
                    mapFase2.put(Integer.parseInt(docAuxAlbum.getString("Año")), docAuxAlbum.getString("Año"));
                }
            }

            if(ListAlbum) {
                for (int a = 0; a < listAlbumAux.size(); a++) {
                    docAuxAlbum = listAlbumAux.get(a);
                    if(!mapFase2.containsValue(docAuxAlbum.getString("Año"))){
                        mapFase2.put(Integer.parseInt(docAuxAlbum.getString("Año")), docAuxAlbum.getString("Año"));
                    }
                }
            }
        }
    }

    public TreeMap<String, ArrayList<String>> consultaFase3Estilo() {

        TreeMap<String, ArrayList<String>> listaImprimir = new TreeMap<String, ArrayList<String>>();
        String autor = "";
        if(fase2.equalsIgnoreCase("Todos")){

            for (int i=0; i<listaDocumentosJSON.size(); i++){
                Document docAux = listaDocumentosJSON.get(i);
                Document docAuxInterprete = (Document) docAux.get("Interprete");
                Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

                if(docAuxNombre.containsKey("NombreC")){
                    autor = docAuxNombre.getString("NombreC");
                }
                if(docAuxNombre.containsKey("NombreG")){
                    autor = docAuxNombre.getString("NombreG");
                }

                String ID = docAuxNombre.getString("Id");

                List<Document> listAlbumAux = null;
                Document docAuxAlbum = null;

                boolean ListAlbum = false, docAlbum = false;

                if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                    listAlbumAux = (List) docAuxInterprete.get("Album");
                    ListAlbum = true;
                }

                if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                    docAuxAlbum = (Document) docAuxInterprete.get("Album");
                    docAlbum = true;
                }

                if(docAlbum){
                    if(!listaImprimir.containsKey(autor)){
                        ArrayList<String> arrayAux = new ArrayList<String>();
                        arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                        listaImprimir.put(autor, arrayAux);
                    } else {
                        ArrayList<String> arrayAux = listaImprimir.get(autor);
                        arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                        listaImprimir.put(autor, arrayAux);
                    }
                }
                if(ListAlbum){
                    for(int x=0; x<listAlbumAux.size(); x++){
                        //System.out.println(docAuxAlbum.getString("NombreA") + "#" + ID);
                        docAuxAlbum = listAlbumAux.get(x);

                        if(!listaImprimir.containsKey(autor)){
                            ArrayList<String> arrayAux = new ArrayList<String>();
                            arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                            listaImprimir.put(autor, arrayAux);
                        } else {
                            ArrayList<String> arrayAux = listaImprimir.get(autor);
                            arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                            listaImprimir.put(autor, arrayAux);
                        }
                    }
                }
            }
        } else {
            for (int i=0; i<listaDocumentosJSON.size(); i++){
                Document docAux = listaDocumentosJSON.get(i);
                Document docAuxInterprete = (Document) docAux.get("Interprete");
                Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

                if(docAuxNombre.containsKey("NombreC")){
                    autor = docAuxNombre.getString("NombreC");
                }
                if(docAuxNombre.containsKey("NombreG")){
                    autor = docAuxNombre.getString("NombreG");
                }

                String ID = docAuxNombre.getString("Id");

                List<Document> listAlbumAux = null;
                Document docAuxAlbum = null;

                boolean ListAlbum = false, docAlbum = false;

                if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                    listAlbumAux = (List) docAuxInterprete.get("Album");
                    ListAlbum = true;
                }

                if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                    docAuxAlbum = (Document) docAuxInterprete.get("Album");
                    docAlbum = true;
                }


                if(docAlbum){
                    if(docAuxAlbum.getString("Año").equals(fase2)){
                      if(!listaImprimir.containsKey(autor)){
                            ArrayList<String> arrayAux = new ArrayList<String>();
                            arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                            listaImprimir.put(autor, arrayAux);
                        } else {
                            ArrayList<String> arrayAux = listaImprimir.get(autor);
                            arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                            listaImprimir.put(autor, arrayAux);
                        }
                    }
                }

                if(ListAlbum){

                    for(int x=0; x<listAlbumAux.size(); x++){
                        docAuxAlbum = listAlbumAux.get(x);
                        if(docAuxAlbum.getString("Año").equals(fase2)){

                            if(!listaImprimir.containsKey(autor)){
                                ArrayList<String> arrayAux = new ArrayList<String>();
                                arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                                listaImprimir.put(autor, arrayAux);
                            } else {
                                ArrayList<String> arrayAux = listaImprimir.get(autor);
                                arrayAux.add(docAuxAlbum.getString("NombreA") + "#" + ID);
                                listaImprimir.put(autor, arrayAux);
                            }
                        }
                    }
                }
            }
        }
        return listaImprimir;
    }

    public void consultaFase4Estilo() {

        mapFase4.clear();

        for (int i = 0; i < listaDocumentosJSON.size(); i++){

            Document docAux = listaDocumentosJSON.get(i);
            Document docAuxInterprete = (Document) docAux.get("Interprete");
            Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

            List<Document> listAlbumAux = null;
            Document docAuxAlbum = null;

            boolean ListAlbum = false, docAlbum = false;

            if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                listAlbumAux = (List) docAuxInterprete.get("Album");
                ListAlbum = true;
            }

            if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                docAuxAlbum = (Document) docAuxInterprete.get("Album");
                docAlbum = true;
            }

            if(docAlbum){
                listAlbumAux.add(docAuxAlbum);
            }


            for (int w=0; w<listAlbumAux.size(); w++){

                docAuxAlbum = listAlbumAux.get(w);

                boolean ListCancion=false, docCancion=false;
                Document docAuxCancion = null;
                List<Document> listCancionAux = null;

                if (docAuxAlbum.get("Cancion") instanceof List<?>){ //Múltiples canciones
                    listCancionAux = (List) docAuxAlbum.get("Cancion");
                    ListCancion = true;
                }

                if (docAuxAlbum.get("Cancion") instanceof Document){ //Sólo una canción
                    docAuxCancion = (Document) docAuxAlbum.get("Cancion");
                    docCancion = true;
                }

                if(docCancion) listCancionAux.add(docAuxCancion);

                for (int e=0; e<listCancionAux.size(); e++){
                    Document aux = listCancionAux.get(e);

                    if (fase2.equalsIgnoreCase("Todos")){
                        if(fase3.equalsIgnoreCase("Todos")){
                            String estilo = aux.getString("-estilo");
                            if(!mapFase4.containsKey(estilo)) mapFase4.put(estilo, mapFase4.size()+1);
                        } else {
                            String ID = docAuxNombre.getString("Id");

                            if(ID.equalsIgnoreCase(autorFase3)){

                                if(docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)){
                                    String estilo = aux.getString("-estilo");
                                    if(!mapFase4.containsKey(estilo)) mapFase4.put(estilo, mapFase4.size()+1);
                                }
                            }
                        }
                    } else {
                        if(fase3.equalsIgnoreCase("Todos")){
                            if(docAuxAlbum.getString("Año").equalsIgnoreCase(fase2)){
                                String estilo = aux.getString("-estilo");
                                if(!mapFase4.containsKey(estilo)) mapFase4.put(estilo, mapFase4.size()+1);
                            }

                        } else {
                            String ID = docAuxNombre.getString("Id");

                            if(ID.equalsIgnoreCase(autorFase3)){

                                if((docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) && (docAuxAlbum.getString("Año").equalsIgnoreCase(fase2))){
                                    String estilo = aux.getString("-estilo");
                                    if(!mapFase4.containsKey(estilo)) mapFase4.put(estilo, mapFase4.size()+1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int consultaFaseFinalEstilo()  {

        int totalCanciones = 0;

        for (int i = 0; i < listaDocumentosJSON.size(); i++){

            Document docAux = listaDocumentosJSON.get(i);
            Document docAuxInterprete = (Document) docAux.get("Interprete");
            Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

            List<Document> listAlbumAux = null;
            Document docAuxAlbum = null;

            boolean ListAlbum = false, docAlbum = false;

            if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                listAlbumAux = (List) docAuxInterprete.get("Album");
                ListAlbum = true;
            }

            if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                docAuxAlbum = (Document) docAuxInterprete.get("Album");
                docAlbum = true;
            }

            if(docAlbum){
                listAlbumAux.add(docAuxAlbum);
            }


            for (int w=0; w<listAlbumAux.size(); w++){

                docAuxAlbum = listAlbumAux.get(w);

                boolean ListCancion=false, docCancion=false;
                Document docAuxCancion = null;
                List<Document> listCancionAux = null;

                if (docAuxAlbum.get("Cancion") instanceof List<?>){ //Múltiples canciones
                    listCancionAux = (List) docAuxAlbum.get("Cancion");
                    ListCancion = true;
                }

                if (docAuxAlbum.get("Cancion") instanceof Document){ //Sólo una canción
                    docAuxCancion = (Document) docAuxAlbum.get("Cancion");
                    docCancion = true;
                }

                if(docCancion) listCancionAux.add(docAuxCancion);

                for (int e=0; e<listCancionAux.size(); e++){
                    Document aux = listCancionAux.get(e);

                    if (fase2.equalsIgnoreCase("Todos")){
                        if(fase3.equalsIgnoreCase("Todos")){
                            if(fase4.equalsIgnoreCase("Todos")){
                                totalCanciones++;
                            } else {
                                if(aux.getString("-estilo").equalsIgnoreCase(fase4)) totalCanciones++;
                            }
                        } else {

                            String ID = docAuxNombre.getString("Id");

                            if(ID.equalsIgnoreCase(autorFase3)){
                                if(fase4.equalsIgnoreCase("Todos")){
                                    if(docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) totalCanciones++;
                                } else {
                                    if((docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) && (aux.getString("-estilo").equalsIgnoreCase(fase4))) totalCanciones++;
                                }
                            }
                        }
                    } else {
                        if(fase3.equalsIgnoreCase("Todos")){
                            if(fase4.equalsIgnoreCase("Todos")){
                                if(docAuxAlbum.getString("Año").equalsIgnoreCase(fase2)) totalCanciones++;
                            } else {
                                if((docAuxAlbum.getString("Año").equalsIgnoreCase(fase2)) && (aux.getString("-estilo").equalsIgnoreCase(fase4))) totalCanciones++;
                            }
                        } else {

                            String ID = docAuxNombre.getString("Id");

                            if(ID.equalsIgnoreCase(autorFase3)){
                                if(fase4.equalsIgnoreCase("Todos")){
                                    if((docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) && (docAuxAlbum.getString("Año").equalsIgnoreCase(fase2))) totalCanciones++;
                                } else {
                                    if((docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) && (docAuxAlbum.getString("Año").equalsIgnoreCase(fase2)) && (aux.getString("-estilo").equalsIgnoreCase(fase4))) totalCanciones++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return totalCanciones;
    }

    //MÉTODOS AUXILIARES
    public ArrayList<String> obtenerCancionesFaseFinalConsulta1 (Document listaCanciones) throws ServletException {

        ArrayList<String> listaFinalConsulta1 = new ArrayList<String>();

        if (listaCanciones != null) {

            String nombreT = listaCanciones.getString("NombreT");
            String duracion = listaCanciones.getString("Duracion");
            String descripcion = listaCanciones.getString("#text").trim();

            if (descripcion.equalsIgnoreCase("")) descripcion = "--";

            listaFinalConsulta1.add(nombreT + " (" + duracion + ", " + descripcion + ")");

        }

        return listaFinalConsulta1;
    }

    public void insertarAnhoAlbum (String anho, String album){

        if((!anho.equalsIgnoreCase("")) && (!album.equalsIgnoreCase(""))){

            if(!mapFase3Consulta1Aux.containsKey(Integer.parseInt(anho))){

                ArrayList <String> arrayTemp = new ArrayList<String>();
                arrayTemp.add(album);

                mapFase3Consulta1Aux.put((Integer.parseInt(anho)), arrayTemp);
            } else {

                ArrayList <String> arrayTemp = mapFase3Consulta1Aux.get(Integer.parseInt(anho));
                arrayTemp.add(album);

                mapFase3Consulta1Aux.put(Integer.parseInt(anho), arrayTemp);
            }

        } else return;
    }

    public void obtenerAlbums (Document docAuxInterprete){

        boolean ListAlbum = false, docAlbum = false;

        List<Document> listAlbumAux = null;
        Document docAuxAlbum = null;

        String anho = "";
        String album = "";

        if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
            listAlbumAux = (List) docAuxInterprete.get("Album");
            ListAlbum = true;
        }

        if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
            docAuxAlbum = (Document) docAuxInterprete.get("Album");
            docAlbum = true;
        }

        if(docAlbum){
            anho = docAuxAlbum.getString("Año");
            album = docAuxAlbum.getString("NombreA");

            insertarAnhoAlbum(anho, album);
        }

        if(ListAlbum){

            for (int a=0; a<listAlbumAux.size(); a++){
                Document docAux2 = listAlbumAux.get(a);

                anho = docAux2.getString("Año");
                album = docAux2.getString("NombreA");

                insertarAnhoAlbum(anho, album);
            }
        }
        anho = "";
        album = "";
    }

    public void obtenerDocsCanciones (Document docAuxAlbum, ArrayList<String> listadoFinalCanciones) throws ServletException{

        boolean ListCancion=false, docCancion=false;
        Document docAuxCancion = null;
        List<Document> listCancionAux = null;

        if (docAuxAlbum.get("Cancion") instanceof List<?>){ //Múltiples canciones
            listCancionAux = (List) docAuxAlbum.get("Cancion");
            ListCancion = true;
        }

        if (docAuxAlbum.get("Cancion") instanceof Document){ //Sólo una canción
            docAuxCancion = (Document) docAuxAlbum.get("Cancion");
            docCancion = true;
        }

        if(ListCancion){

            for(int p=0; p<listCancionAux.size(); p++){
                listadoFinalCanciones.addAll(obtenerCancionesFaseFinalConsulta1(listCancionAux.get(p)));
            }
        }
        if(docCancion){
            listadoFinalCanciones.addAll(obtenerCancionesFaseFinalConsulta1(docAuxCancion));
        }

        return;

    }

    public void comprobadorFase3ConsultaFinalAlbum(Document docAuxInterprete, ArrayList<String> listadoFinalCanciones) throws ServletException {

        if (fase3.equalsIgnoreCase("Todos")) {

            List<Document> listAlbumAux = null;
            Document docAuxAlbum = null;

            boolean ListAlbum = false, docAlbum = false;

            if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                listAlbumAux = (List) docAuxInterprete.get("Album");
                ListAlbum = true;
            }

            if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                docAuxAlbum = (Document) docAuxInterprete.get("Album");
                docAlbum = true;
            }

            if(docAlbum){
                obtenerDocsCanciones(docAuxAlbum, listadoFinalCanciones);
            }

            if(ListAlbum) {
                for (int a = 0; a < listAlbumAux.size(); a++) {
                    docAuxAlbum = listAlbumAux.get(a);
                    obtenerDocsCanciones(docAuxAlbum, listadoFinalCanciones);
                }
            }

        } else {

            List<Document> listAlbumAux = null;
            Document docAuxAlbum = null;

            boolean ListAlbum = false, docAlbum = false;

            if (docAuxInterprete.get("Album") instanceof List<?>){ //Múltiples álbums
                listAlbumAux = (List) docAuxInterprete.get("Album");
                ListAlbum = true;
            }

            if (docAuxInterprete.get("Album") instanceof Document){ //Sólo un álbum
                docAuxAlbum = (Document) docAuxInterprete.get("Album");
                docAlbum = true;
            }


            if(docAlbum){
                if(docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) obtenerDocsCanciones(docAuxAlbum, listadoFinalCanciones);
            }

            if(ListAlbum) {
                for (int a = 0; a < listAlbumAux.size(); a++) {
                    docAuxAlbum = listAlbumAux.get(a);
                    if(docAuxAlbum.getString("NombreA").equalsIgnoreCase(fase3)) obtenerDocsCanciones(docAuxAlbum, listadoFinalCanciones);
                }
            }
        }
    }


    //PARSEO DE DOCUMENTOS DE LA BASE DE DATOS
    public void parseoJSONDB(List<Document> foundDocument){

        for (int i=0; i<foundDocument.size(); i++){

            Document docAux = (Document) foundDocument.get(i);

            listaErrores.add("<b>Ocurrencias documento " + (i+1) + ":</b><br>");

            if(parsearJSON(docAux)){
                listaDocumentosJSON.add(docAux);
            }

            listaErrores.add("<br>");
        }
    }

    public boolean parsearJSON(Document documento) {

        List<Document> listAlbumAux = null;
        Document docAuxAlbum = null;
        boolean ListAlbum = false, docAlbum = false;

        //COMPROBACIONES DE CAMPOS NombreC/NombreG, NombreA, NombreT y -estilo vacíos
        //Comprobación Nombre
        Document docAuxInterprete = (Document) documento.get("Interprete");
        Document docAuxNombre = (Document) docAuxInterprete.get("Nombre");

        if(docAuxNombre.containsKey("NombreC")){
            if(docAuxNombre.getString("NombreC").equals("")) {
                listaErrores.add("El campo NombreC de este documento está vacío");
                return false;
            }
        }
        if(docAuxNombre.containsKey("NombreG")){
            if(docAuxNombre.getString("NombreG").equals("")) {
                listaErrores.add("El campo NombreG de este documento está vacío");
                return false;
            }
        }


        //Comoprobación Album, Cancion y Estilo vacío
        if (docAuxInterprete.get("Album") instanceof List<?>){
            listAlbumAux = (List) docAuxInterprete.get("Album");
            ListAlbum = true;
        }

        if (docAuxInterprete.get("Album") instanceof Document){
            docAuxAlbum = (Document) docAuxInterprete.get("Album");
            docAlbum = true;
        }

        if((!ListAlbum) && (!docAlbum)) {
            listaErrores.add("No hay álbumes en este documento.");
            return false; //Si no hay album, error.
        }
        else{
            if(docAlbum){ //Sólo un Álbum
                if(!comprobarAlbumCancionEstilo(docAuxAlbum)) return false;

                if(docAuxAlbum.getString("Año") == null) {
                    listaErrores.add("No existe el campo Año en algún lugar de este documento.");
                    return false;
                }

                try{
                    Integer.parseInt(docAuxAlbum.getString("Año"));
                } catch (Exception e) {
                    listaErrores.add("Un campo Año no tiene un valor válido.");
                    return false;}
            }

            //Muchos álbums
            if(ListAlbum){
                for(int x=0; x<listAlbumAux.size(); x++){
                    if(!comprobarAlbumCancionEstilo(listAlbumAux.get(x))) return false;

                    if(listAlbumAux.get(x).getString("Año") == null) {
                        listaErrores.add("No existe el campo Año en algún lugar de este documento.");
                        return false;
                    }

                    try{
                        Integer.parseInt(listAlbumAux.get(x).getString("Año"));
                    } catch (Exception e) {
                        listaErrores.add("Un campo Año no tienen un valor válido");
                        return false;}

                }
            }
        }

        //Comprobaciones de tipo de datos en Nacionalidad, Año (hecho arriba), Duración, Número de temas concordante y tipo de Álbum
        //Nacionalidad
        if((docAuxInterprete.getString("Nacionalidad") == null) || (docAuxInterprete.getString("Nacionalidad").equalsIgnoreCase(""))) {
            listaErrores.add("El campo Nacionalidad está vacío para este documento.");
            return false;
        }
        else {
            if((!docAuxInterprete.getString("Nacionalidad").equalsIgnoreCase("Español")) && (!docAuxInterprete.getString("Nacionalidad").equalsIgnoreCase("Italiano")) && (!docAuxInterprete.getString("Nacionalidad").equalsIgnoreCase("Americano"))) {
                listaErrores.add("El campo Nacionalidad es inocrrecto para este documento.");
                return false;
            }
        }

        return true;
    }

    public boolean comprobarAlbumCancionEstilo(Document docAuxAlbum){

        int numeroCanciones = -100;

        if((!docAuxAlbum.getString("-tipo").equalsIgnoreCase("Nuevo")) && (!docAuxAlbum.getString("-tipo").equalsIgnoreCase("Recopilatorio")) && (!docAuxAlbum.getString("-tipo").equalsIgnoreCase("Mixto"))) {
            listaErrores.add("El tipo de un álbum no tiene un valor correcto");
            return false;
        }

        if(docAuxAlbum.getString("-temas") != null){
            try{
                numeroCanciones = Integer.parseInt(docAuxAlbum.getString("-temas"));
            } catch (Exception e) {
                listaErrores.add("El atributo temas no tiene un valor correcto.");
                return false;}
        }


        if(docAuxAlbum.getString("NombreA").equalsIgnoreCase("") || docAuxAlbum.getString("NombreA") == null) {
            listaErrores.add("El campo NombreA de un álbum está vacío.");
            return false;
        }
        else{

            List<Document> listCancionAux = null;
            Document docCancionAux = null;
            boolean hayCanciones = false;

            if(docAuxAlbum.get("Cancion") instanceof List<?>){
                listCancionAux = (List) docAuxAlbum.get("Cancion");

                if(numeroCanciones != -100){
                    if(numeroCanciones != listCancionAux.size()) {
                        listaErrores.add("El número de canciones de un atributo -temas no coincide con el número real de canciones.");
                        return false;
                    }
                }

                for (int i = 0; i<listCancionAux.size(); i++){
                    if((listCancionAux.get(i).getString("NombreT") == null) || (listCancionAux.get(i).getString("NombreT").equalsIgnoreCase(""))) {
                        listaErrores.add("Un campo NombreT está vacío.");
                        return false;
                    }
                    if((listCancionAux.get(i).getString("-estilo") == null) || (listCancionAux.get(i).getString("-estilo").equalsIgnoreCase(""))) {
                        listaErrores.add("Un -estilo está vacío.");
                        return false;
                    }

                    if((listCancionAux.get(i).getString("Duracion") == null) || (!listCancionAux.get(i).getString("Duracion").contains(":"))) {
                        listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                        return false;
                    }

                    String [] aux = listCancionAux.get(i).getString("Duracion").split(":");
                    if(aux.length != 2) {
                        listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                        return false;
                    }

                    try{
                        for (int w=0; w<aux.length; w++){
                            Integer.parseInt(aux[w]);
                        }
                    } catch (Exception e){
                        listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                        return false;}
                }
                hayCanciones = true;
            }

            if(docAuxAlbum.get("Cancion") instanceof Document){
                docCancionAux = (Document) docAuxAlbum.get("Cancion");

                if(numeroCanciones != -100){
                    if(numeroCanciones != 1) {
                        listaErrores.add("El número de canciones de un atributo -temas no coincide con el número real de canciones.");
                        return false;
                    }
                }

                if((docCancionAux.getString("NombreT") == null) || (docCancionAux.getString("NombreT").equalsIgnoreCase(""))) {
                    listaErrores.add("Un campo NombreT está vacío.");
                    return false;
                }
                if((docCancionAux.getString("-estilo")) == null || (docCancionAux.getString("-estilo").equalsIgnoreCase(""))) {
                    listaErrores.add("Un -estilo está vacío.");
                    return false;
                }

                if((docCancionAux.getString("Duracion") == null) || (!docCancionAux.getString("Duracion").contains(":"))) {
                    listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                    return false;
                }

                String [] aux = docCancionAux.getString("Duracion").split(":");
                if(aux.length != 2) {
                    listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                    return false;
                }

                try{
                    for (int w=0; w<aux.length; w++){
                        Integer.parseInt(aux[w]);
                    }
                } catch (Exception e){
                    listaErrores.add("Un campo Duracion no tiene un valor correcto.");
                    return false;}

                hayCanciones = true;
            }
            if(!hayCanciones) {
                listaErrores.add("Hay un Album sin Cancion.");
                return false;
            }
        }
        return true;
    }

}
