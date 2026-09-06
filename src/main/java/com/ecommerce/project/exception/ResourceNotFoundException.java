package com.ecommerce.project.exception;

public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String field;
    String fieldName;

    Long fieldID;

    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        super(String.format("%s not found with %s: %s", resourceName,field,fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    public ResourceNotFoundException(String resourseName,String field,Long fieldID){
        super(String.format("%s not found with %s: %s", resourseName,field,fieldID));
        this.resourceName = resourseName;
        this.field = field;
        this.fieldID = fieldID;
    }

    public ResourceNotFoundException() {
    }
}
