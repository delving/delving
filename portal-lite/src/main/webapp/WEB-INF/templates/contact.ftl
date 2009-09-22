<#import "spring.ftl" as spring />
<#assign thisPage = "contact.html"/>
<#include "inc_header.ftl"/>

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">


<div class="togglers" style="display:none;">
	<div id="effects" class="ui-widget-content ui-corner-all">
		<h3 class="ui-widget-header ui-corner-all">Show</h3>

	</div>
</div>


        <div class="yui-b"><br  /><br  />
            <h1><@spring.message 'Contacts_t' /></h1>
        </div>



        <div  class="yui-b">
            <div  class="yui-g">
                <div  class="yui-g first">

                <div class="toggler" style="position:absolute; top: 0px; left: 180px; width: 400px; font-size: 12px;">
                    <div id="effect" class="ui-widget-content ui-corner-all">
                        <h3 class="ui-widget-header ui-corner-all"><@spring.message 'SendUsFeedback_t' /></h3>
                        <p>
                            <strong>Eng</strong><br/>
                            Thank you very much for your feedback.
                            If your feedback requires a response a member of the Europeana team will get back to you as soon as possible.
                            <strong>For faster response feedback should be submitted in English.</strong>
                         </p>
                        <p>
                            <strong>Deu</strong><br/>
                            Vielen Dank f&#252;r Ihr Feeback.
                            Falls Ihr Feedback eine Antwort erfordert, wird sich ein Mitglied des Europeanateams schnellstm&#246;glich mit Ihnen in Verbindung setzen.
                            <strong>Bei Feedback in Englisch erhalten Sie schneller eine Antwort.</strong>
                        </p>
                        <p>
                            <strong>Esp</strong><br/>
                            Muchas gracias por sus comentarios.
                            Si sus comentarios requieren una respuesta, el personal de Europeana se pondr&#225; en contacto con Usted a la mayor brevedad posible.
                            <strong>Si precisa de una respuesta m&#225;s r&#225;pida, por favor remita sus comentarios en ingl&#233;s.</strong>
                         </p>
                        <p>
                            <strong>Fra</strong><br/>
                            Merci pour vos commentaires.
                            Pour toutes questions un membre de l'&#233;quipe d'Europeana ce fera un plaisir de vous r&#233;pondre au plus t&#244;t.
                            <strong>Pour un traitement plus rapide de vos questions, merci de bien vouloir les soumettre en anglais.</strong>
                          </p>
                        <p>
                            <strong>Ita</strong><br/>
                            Grazie per la sua opinione.
                            Nel caso sia necessaria una risposta, un componente del team di Europeana vi contatter&#224; al pi&#249; presto.
                            <strong>Per ottenere una risposta pi&#249; rapida si consiglia di scrivere in inglese.</strong>
                          </p>
                        <p>
                            <strong>Pol</strong><br/>
                            Dzi&#281;kujemy za przedstawienie opinii.
                            Je&#347;li opinia wymaga odpowiedzi, zesp&#243;&#322; Europeany skontaktuje si&#281; z Panem/Pani&#261; w mo&#380;liwie najbli&#380;szym czasie.
                            <strong>Aby skr&#243;ci&#263; okres oczekiwania na odpowied&#378;, prosimy o sformu&#322;owanie opinii po angielsku.</strong>

                        </p>
                        <p></p>
                    </div>
                </div>

                    <div   id="contact">
                    <h2>&#160;<#--Contact information--></h2><br/>
                    <h4>Europeana.eu</h4>
                    <p>
                    c/o the Koninklijke Bibliotheek
                    <br />
                    National Library of the Netherlands
                    <br />
                    PO Box 90407
                    <br />
                    2509 LK The Hague
                    </p>
                    <h4>To be added to the press list or for general communications</h4>
                    <p><a  href="#" onclick="return ContactMe('info','europeana.eu');">Jonathan Purday</a><br  />
                    Phone +31 [0] 70 314 0684
                    </p>

                    <h4>To contribute content to Europeana</h4>
                    <p>
                    <a  href="#" onclick="return ContactMe('info','europeana.eu');">Go Sugimoto</a>, interoperability manager<br />
                    </p>

                </div>
                </div>
                <div class="yui-g">

                    <h2><@spring.message 'SendUsFeedback_t' /></h2>

                    <#if command.submitMessage??>

                        <p class="success">${command.submitMessage}</p>

                    <#else>

                    <form action="" method="post" id="feedback-form" name="feedback-form">
                        <fieldset>

                            <legend>Feedback form</legend>

                           <p>
                            <label for="email">Your email address:</label><br/>
                            <input type="text" class="required txt" name="email" id="email" maxlength="50" value="${command.email}"/>
                            <br>
                            <@spring.bind "command.email" />
                           <#list spring.status.errorMessages as error><span class="fg-red"> <i>${error}</i></span> <br> </#list>
                            </p>

                            <label for="feedback">Your feedback or comments:</label>
                            <textarea name="feedbackText" id="feedback" cols="" rows="" class="required">${command.feedbackText}</textarea><br  />
                            <@spring.bind "command.feedbackText" />
                            <#list spring.status.errorMessages as error><span class="fg-red"> <i>${error}</i></span> <br> </#list>

                            <input  type="submit" class="button" value="Send"/>

                        </fieldset>
                    </form>

                    </#if>
                </div>
            </div>
        </div>
    </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>
        </div>
    </div>
   <div id="ft">
	   <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>
