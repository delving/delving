$(document).ready(function() {
    $("#feedback")
    .hover(
			function(){
				$("#thankyou").fadeIn("slow");
			},
			function(){
				$("#thankyou").fadeOut("slow");
			}
	);
});
