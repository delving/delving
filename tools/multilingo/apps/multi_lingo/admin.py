from django.contrib import admin

from models import TranslatePage, MediaFile, Language




def make_active(modeladmin, request, queryset):
    queryset.update(active=True)
make_active.short_description = "Production activate selected pages"

def make_inactive(modeladmin, request, queryset):
    queryset.update(active=False)
make_inactive.short_description = "Production inactivate selected pages"




class TranslatePageAdmin(admin.ModelAdmin):
    list_display = ('file_name', 'active')
    actions = [make_active, make_inactive]

admin.site.register(TranslatePage, TranslatePageAdmin)


admin.site.register(MediaFile)

admin.site.register(Language)