// groovy script

import groovy.xml.MarkupBuilder 

class HelloWorld {


	def sayHello(String name){
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer) 
		
		xml.records() { 
		    car(name:'HSV Maloo', make:'Holden', year:2006) {
		        country('Australia')
		        record(type:'speed', 'Production Pickup Truck with speed of 271kph')
		    }
		    car(name:'Royale', make:'Bugatti', year:1931) {
		        country('France')
		        record(type:'price', 'Most Valuable Car at $15 million')
		    }
		} ;
		
		return writer.toString() ;
	
		// return "hello " + name ; 
	}
} ;

new HelloWorld() ;