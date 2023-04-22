package org.wsh;

import java.util.ResourceBundle;

public class Main {
    public static void main(String[] args) {
        ResourceBundle bundle = ResourceBundle.getBundle("jdbc");
        System.out.println(bundle.getString("driver"));
    }
}
