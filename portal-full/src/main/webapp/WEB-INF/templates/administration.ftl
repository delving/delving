<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html">
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl">
<body>
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <div class="yui-g" id="mainContent">

              <h1>BEWARE: ${administrator.email} = GOD</h1>

              <div class="yui-u first">
                <form>
                 <legend>Edit users:</legend>
                 <fieldset>
                  <label for="searchField">Search For :</label>
                  <input name="searchField" type="text">
                  <input name="action" type="hidden" value="fetchUser"/>
                  <input name="button" type="submit" value="Search" class="button"/>
                 </fieldset>
                </form>
              </div>
              <#if userList?exists>              
                <div class="yui-u">
                    <#list userList as user>
                        <strong>${user.email}</strong>
                        <#if user.enabled>
                        <a href="?action=setEnabled&id=${user.id}&enabled=false">disable</a>
                        <#else>
                        <a href="?action=setEnabled&id=${user.id}&enabled=true">enable</a>
                        </#if>
                        <#if user.role="ROLE_USER">
                        <a href="?action=setAdministrator&id=${user.id}&administrator=true">make administrator</a>
                        <#else>
                        <a href="?action=setAdministrator&id=${user.id}&administrator=false">make user</a>
                        </#if>
                        <br>
                    </#list>
                </div>
              </#if>
            </div>

            <div class="yui-g" id="mainContent">
              <div class="yui-u first">
                <form>
                 <legend>Reindex everything by typing<br>'${administrator.email} is certain!':</legend>
                 <fieldset>
                  <label for="searchField">Are you certain?:</label>
                  <input name="certaintyField" type="text" value="">
                  <input name="action" type="hidden" value="reindexEverything"/>
                  <input name="button" type="submit" value="Reindex" class="button"/>
                 </fieldset>
                </form>
              </div>
            </div>

            <div class="yui-g" id="mainContent">
              <div class="yui-u first">
                <form>
                 <legend>Reindex all records that have editor picks:</legend>
                 <fieldset>
                  <input name="action" type="hidden" value="reindexEditorPicks"/>
                  <input name="button" type="submit" value="Reindex" class="button"/>
                 </fieldset>
                </form>
              </div>
            </div>

            <div class="yui-g" id="mainContent">
              <div class="yui-u first">
                <form>
                 <legend>Reindex all records that have social tags:</legend>
                 <fieldset>
                  <input name="action" type="hidden" value="reindexSocialTags"/>
                  <input name="button" type="submit" value="Reindex" class="button"/>
                 </fieldset>
                </form>
              </div>
            </div>

            <div class="yui-g" id="mainContent">
              <div class="yui-u first">
                <#assign keys = countMap?keys>
                <h2>Statistics</h2>
                <#list keys as key>
                    <p>Records in <strong>${key}</strong>: ${countMap[key]}<br/>
                </#list>
              </div>
            </div>

            <div class="yui-g" id="mainContent">
              <div class="yui-u first">
                <h2>Collections</h2>
                <#if collectionsEnabled?has_content>
                    <form>
                     <fieldset>
                        <select name="collection" id="collection">
                          <option value="-1">Choose an enabled collection to disable</option>
                        <#list collectionsEnabled as collection>
                          <option value="${collection.id?string("0")}">${collection.name}</option>
                        </#list>
                        </select>
                        <input name="action" type="hidden" value="setCollectionEnabled"/>
                        <input name="enabled" type="hidden" value="false"/>
                        <br/>
                        <input name="button" type="submit" value="Disable" class="button"/>
                     </fieldset>
                    </form>
                </#if>
                <#if collectionsDisabled?has_content>
                    <form>
                     <fieldset>
                        <select name="collection" id="collection">
                          <option value="-1">Choose a disabled collection to enable</option>
                        <#list collectionsDisabled as collection>
                          <option value="${collection.id?string("0")}">${collection.name}</option>
                        </#list>
                        </select>
                        <input name="action" type="hidden" value="setCollectionEnabled"/>
                        <input name="enabled" type="hidden" value="true"/>
                        <br/>
                        <input name="button" type="submit" value="Enable" class="button"/>
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
