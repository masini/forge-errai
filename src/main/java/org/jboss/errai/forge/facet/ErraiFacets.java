package org.jboss.errai.forge.facet;


import org.jboss.errai.forge.facet.ui.ErraiUIFacet;

/**
 * @author pslegr
 */
public enum ErraiFacets {
    ERRAI_BUS_FACET("Errai Bus Facet",ErraiBusFacet.class),
    ERRAI_CDI_FACET("Errai CDI Facet",ErraiCDIFacet.class),
    ERRAI_JAXRS_FACET("Errai Jaxrs Facet",ErraiJaxrsFacet.class),
    ERRAI_UI_FACET("Errai UI Facet",ErraiUIFacet.class);
    		
    private String name;
    private Class<? extends ErraiBaseFacet> facet;

    private ErraiFacets(String name, Class<? extends ErraiBaseFacet> facet) {
        this.name = name;
        this.facet = facet;
    }

    public Class<? extends ErraiBaseFacet> getFacet() {
        return facet;
    }

    @Override
    public String toString() {
        return name;
    }
}
