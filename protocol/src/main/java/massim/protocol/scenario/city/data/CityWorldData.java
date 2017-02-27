package massim.protocol.scenario.city.data;

import massim.protocol.WorldData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds the complete world state of the city simulation.
 */
@XmlRootElement(name="world")
@XmlAccessorType(XmlAccessType.NONE)
public class CityWorldData extends WorldData {

    // TODO add all the data

    /**
     * For jaxb.
     */
    private CityWorldData(){
        super();
    }

    public CityWorldData(String something){

    }
}
