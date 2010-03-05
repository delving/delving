"""
 Generic utility functions
"""

def dict_2_django_choice(d):
    lst = []
    for key in d:
        lst.append((key, d[key]))
    return lst
