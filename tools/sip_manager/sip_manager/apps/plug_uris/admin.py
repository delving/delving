
from django.contrib import databrowse

import models


databrowse.site.register(models.Uri)
databrowse.site.register(models.UriSource)
