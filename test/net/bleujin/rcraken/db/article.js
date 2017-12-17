new function(){

	this.listBy = function() {
		return session.pathBy('/articles').children().toRows('artId, artSubject') ;
	} 
	
} ;

