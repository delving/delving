def translator_allowed(user, lang):
    """Europeana specific modification
    Only allow translators to modify the languages they are responsible for
    """
    a = user.get_all_permissions()
    b = user.has_perm('rosetta.%s' % lang) or user.has_perm('rosetta.all_langs')
    return b

