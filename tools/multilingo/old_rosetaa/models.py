from django.db import models
# Create your models here.

from django.conf import settings

def generate_permissions():
    perms = [("all_langs", "All languages")]
    for lang, lang_long in settings.LANGUAGES:
        perms.append( (lang, lang))
    return perms


class Translator(models.Model):
    # ...
    class Meta:
        permissions = generate_permissions()
