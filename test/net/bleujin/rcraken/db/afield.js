new function(){

	this.createWith = function(afieldId, afieldNm) {
		session.tranScript(function(wsession){
			wsession.pathBy('/afields/' + afieldId).property('afieldId', afieldId).property('afieldNm', afieldNm).merge() ;
		}) ;
		return 1 ;
	}, 

	this.removeAllWith = function() {
		session.tranScript(function(wsession){
			wsession.pathBy('/afields').removeChild() ;
		}) ;
		return 1 ;
	}, 
	
	
	this.listBy = function(skip, offset) {
		return session.pathBy('/afields').children().stream().skip(skip).limit(offset).toRows('afieldId, afieldNm') ;
	}, 
	
	this.batchWith = function(ids, names){
		session.tranScript(function(wsession){
			for(var i in ids){
				wsession.pathBy('/afields/' + ids[i]).property('afieldId', ids[i]).property('afieldNm', names[i]).merge() ;
			}
		}) ;
		
		return ids.length ;
	}
} ; 

