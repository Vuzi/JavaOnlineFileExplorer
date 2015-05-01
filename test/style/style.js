 $(document).ready(function() {

   $(".hideAll").click(function() {
   	 $(".showLink").each(function() {
   	 	$($(this).attr("href")).slideUp();
   	 })
     return false;
   });

   $(".showLink").click(function() {
   	 var elem = $(this).attr("href");
     
   	 if($(elem).is(":hidden")) {
   	 	$(elem).slideDown();
   	 } else {
   	 	$(elem).slideUp();
   	 }

     return false;
   });

 });