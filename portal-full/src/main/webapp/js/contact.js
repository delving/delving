$(function() {

		//run the currently selected effect

		function runEffect(){
			//get effect type from
			var selectedEffect = "blind";

			//most effect types need no options passed by default
			var options = {};
			//check if it's scale, transfer, or size - they need options explicitly set
			if(selectedEffect == 'scale'){  options = {percent: 100}; }
			else if(selectedEffect == 'transfer'){ options = { to: "#button", className: 'ui-effects-transfer' }; }
			else if(selectedEffect == 'size'){ options = { to: {width: 280,height: 185} }; }

			//run the effect
			$("#effect").show(selectedEffect,options,500,callback);
		};

		//callback function to bring a hidden box back
		function callback(){
			setTimeout(function(){
				$("#effect:visible").removeAttr('style').hide().fadeOut();
			}, 15000);
		};

		//set effect
        // once
        var once = true ;
		$("#feedback").focus(function() {
            if ( once )
            {
                once = false ;
			    runEffect();
            }
			return false;
		});

        // User may decide
        $("#close").click(function() {
            $("#effect").hide();
		});

		$("#effect").hide();
});
