#!/bin/bash
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

# Recorre todos los ficheros .pdf del directorio, y los
# convierte a eps utilizando el programa pdftops.

# Se basó originalmente en:

# http://www.tug.org/pipermail/texhax/2003-June/000396.html
# http://amath.colorado.edu/documentation/LaTeX/reference/figures.html

# aunque ya no tiene nada que ver :-)


# Convierte a .eps el fichero .pdf recibido como primer parámetro.
# Si hay algún problema devuelve 1; en otro caso, devuelve 0
function convert {
    f=$1

    # Construimos el nombre del fichero con extensión .eps
    eps=$(echo $f | sed s/pdf$/eps/)

    echo -n "$f --> $eps ... "

    # Si el fichero $eps existe y es más nuevo que el original,
    # nos ahorramos la conversión.
    [ $eps -nt $f ] && echo $eps está actualizado. && exit 0

    # Hacemos la comprobación de si está instalado pdftops
    # en un lugar "tan profundo" para que sólo falle si
    # realmente lo vamos a usar. Esto significa que vamos
    # a comprobarlo por cada imagen... La otra opción
    # habría sido comprobarlo directamente en updateAll.sh
    # pero en ese caso, si no tenemos el pdftops, pero tampoco
    # tenemos imágenes que convertir, acabaríamos con un 
    # "error" que no resulta problemático.
    # De hecho lo hacemos incluso después de haber comprobado
    # que no tenemos ya el .eps actualizado, para no quejarnos
    # si el fichero existe realmente.
    if ! which pdftops > /dev/null; then
        echo "El programa pdftops no está disponible." > /dev/stderr
        echo "No pueden convertirse las imagenes vectoriales." > /dev/stderr
	exit 1
    fi
    pdftops -eps $f $eps >/dev/null 2>&1 && echo Hecho. || (echo ERROR. && rm -f $eps && exit 1)

}



for f in *.pdf; do
    [ ! -e $f ] && continue
    # Necesario porque el for mete en f un
    # *.pdf si no hay ninguno, a si es que nos
    # aseguramos de que el fichero existe.

    # Convertimos todos los ficheros encontrados
    if ! convert $f; then
        exit 1
    fi
done
