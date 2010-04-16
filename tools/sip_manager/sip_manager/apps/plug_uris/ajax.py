from dajax.core import Dajax
def multiply(request, a, b):
	dajax = Dajax()
	result = int(a) * int(b)
	dajax.assign('#result','value',str(result))
	return dajax.render()