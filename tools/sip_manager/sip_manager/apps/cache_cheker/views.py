# Create your views here.
from django import forms

class CacheForm(forms.Form):
    org_url = forms.CharField(max_length=100)


def index(request):
    if request.method == 'POST': # If the form has been submitted...
        form = CacheForm(request.POST) # A form bound to the POST data
        if form.is_valid(): # All validation rules pass
            # Process the data in form.cleaned_data
            # ...
            return HttpResponseRedirect('/thanks/') # Redirect after POST
    else:
        form = ContactForm() # An unbound form

    return render_to_response('contact.html', {
        'form': form,
    })
