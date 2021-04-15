import org.w3c.dom.Element;


public class Server {
    protected String type;
    protected int limit, bootupTime, coreCount, memory, disk;
    protected double hourlyRate;
    
    protected Server(Element element){
        type = element.getAttribute("type");
        limit = Integer.parseInt(element.getAttribute("limit"));
        bootupTime = Integer.parseInt(element.getAttribute("bootupTime")) ;
        hourlyRate = Double.parseDouble(element.getAttribute("hourlyRate")) ;
        coreCount = Integer.parseInt(element.getAttribute("coreCount")) ;
        memory = Integer.parseInt(element.getAttribute("memory")) ;
        disk = Integer.parseInt(element.getAttribute("disk")) ;
    } 
}
