package edu.jsu.mcis.cs408.simplechat;

public class Controller extends AbstractController {

    public static final String ELEMENT_OUTPUT_PROPERTY = "Output";

    public void sendGetRequest() {
        invokeModelMethod("sendGetRequest", null);
    }

    public void sendPostRequest() {
        invokeModelMethod("sendPostRequest", null);
    }
    public void sendDeleteRequest(){invokeModelMethod("sendDeleteRequest", null);}

}
