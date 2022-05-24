package com.fortify.cli.tools.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class ToolPackage{
    @Getter @Setter private String Name;
    @Getter @Setter private String DefaultVersion;
    @Getter @Setter private ArrayList<ToolPackageVersion> Versions;

    public ToolPackage(String Name, String DefaultVersion, ArrayList<ToolPackageVersion> Versions){
        this.Name = Name;
        this.DefaultVersion = DefaultVersion;
        this.Versions = Versions;
    }

    public ToolPackage(){}
}