import org.w3c.dom.Element;

//Store information related to server types
public class ServerTypes {

    //Servers of each type are identified using a unique ascending integer starting from 0
    //DEFAULT_ID is set to starting server ID
    private static final int DEFAULT_ID = 0;

    protected String type;
    protected int limit, bootupTime, coreCount, memory, disk, id;
    protected double hourlyRate;
    
    protected ServerTypes(Element element){
        type = element.getAttribute("type");
        limit = Integer.parseInt(element.getAttribute("limit"));
        bootupTime = Integer.parseInt(element.getAttribute("bootupTime")) ;
        hourlyRate = Double.parseDouble(element.getAttribute("hourlyRate")) ;
        coreCount = Integer.parseInt(element.getAttribute("coreCount")) ;
        memory = Integer.parseInt(element.getAttribute("memory")) ;
        disk = Integer.parseInt(element.getAttribute("disk")) ;
        id= DEFAULT_ID;
    } 
}
