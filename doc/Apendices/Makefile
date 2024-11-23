#----------------------------------------------------------
#
# Copyright 2009 Pedro Pablo Gomez-Martin,
#                Marco Antonio Gomez-Martin
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
#----------------------------------------------------------


#
# Borra todos los ficheros intermedios.
#

clean:
	@echo Borrando los ficheros de log...
	@rm -f *.log
	@echo Borrando los ficheros auxiliares...
	@rm -f *.aux
	@rm -f *.out
	@rm -f *.exa
	@echo Borrando los ficheros de generación de índices de palabras...
	@rm -f *.idx
	@rm -f *.ilg
	@rm -f *.ind
	@echo Borrando los ficheros de generación de tablas de contenidos...
	@rm -f *.toc
	@rm -f *.lof
	@rm -f *.lot
	@echo Borrando los ficheros de salida...
	@rm -f *.dvi
	@rm -f *.ps
	@echo Borrando los ficheros intermedios de BibTeX...
	@rm -f *.bbl
	@rm -f *.blg

#
# Borra todos los ficheros intermedios, las copias de seguridad
# y los ficheros generados
#
distclean: clean
	@echo Borrando los ficheros de salida...
	@rm -f *.pdf
	@echo Borrando las copias de seguridad de los editores...
	@rm -f *~
	@rm -f *.backup
