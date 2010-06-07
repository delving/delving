def translator_allowed(user, lang):
    """Europeana specific modification
    Only allow translators to modify the languages they are responsible for

    modify rosetta/views.py (aprox #239)
    for language in settings.LANGUAGES:
        # Patch to only allow translator to handle assigned language
        if not gen_utils.translator_allowed(request.user, language[0]):
            continue

    """
    a = user.get_all_permissions()
    b = user.has_perm('rosetta.%s' % lang) or user.has_perm('rosetta.all_langs')
    return b

