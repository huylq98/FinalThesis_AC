/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package bean;

import javax.inject.Named;

@Named
public class ControllerBean {
   
   public ControllerBean() {
      System.out.println("HelloWorld started!");
   }
	
   public String getMessage() {
      return "Hello World!";
   }
}