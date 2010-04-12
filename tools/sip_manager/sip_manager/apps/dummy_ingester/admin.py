"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)



"""

from django import forms
from django.contrib import admin
from django.contrib import databrowse

import models




databrowse.site.register(models.Aggregator)
databrowse.site.register(models.Provider)
databrowse.site.register(models.DataSet)
databrowse.site.register(models.Request)



class MyRequestAdminForm(forms.ModelForm):
    class Meta:
        model = models.Request

    def clean_file_name(self):
        fname = self.find_ingest_filename(self.cleaned_data["file_name"])
        if not fname:
            raise forms.util.ValidationError('Did not match any existing file')
        return fname # self.cleaned_data["file_name"]

    def find_ingest_filename(self, s_in):
        s = s_in.lower()
        fname = ''
        for dir_entry in os.listdir(settings.DUMMY_INGEST_DIR):
            if dir_entry.lower().find(s) == 0:
                fname = dir_entry
                break
        return fname



class RequestAdmin(admin.ModelAdmin):
    form = MyRequestAdminForm




admin.site.register(models.Aggregator)
admin.site.register(models.Provider)
admin.site.register(models.DataSet)
admin.site.register(models.Request, RequestAdmin)
