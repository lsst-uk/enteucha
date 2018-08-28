import math

#
#  Computes direction cosines from spherical polars
#  (trigonometry cribbed from SLALIB) given ra and
#  dec in radians, returns cartesian unit vectors
#  cx, cy, cz each in the range 0 to 1.
#
def sphericalToCartesian(ra, dec):
    
    cosd = math.cos(dec)
    cx = math.cos(ra) * cosd
    cy = math.sin(ra) * cosd
    cz = math.sin(dec)
    return cx, cy, cz
   
# 
#  Angular distance between 2 NEARBY(!) points on the
#  celestial sphere (coords in radians), returns
#  angular distance in radians
#
def angularDistance(ra1, dec1, ra2, dec2):
     
    cx1, cy1, cz1 = sphericalToCartesian(ra1, dec1)
    cx2, cy2, cz2 = sphericalToCartesian(ra2, dec2)
    # see https://arxiv.org/pdf/cs/0408031.pdf
    r2 = (cx1 - cx2)*(cx1 - cx2) + (cy1 - cy2)*(cy1 - cy2) + (cz1 - cz2)*(cz1 - cz2)
    return 2.0*math.asin(math.sqrt(r2)/2.0)
    
oneArcsecInRads = math.pi / (180.0 * 3600.0)
# angular distance between 0h 0deg and plus one arcsec in both:
print angularDistance(0.0, 0.0, oneArcsecInRads, oneArcsecInRads)*180.0*3600.0 / math.pi
# ... answer should be sqrt(2) arcsec
# another test: one arcsecond off the North Celestial Pole (at any value of ra)
print angularDistance(math.pi, math.pi/2.0, math.pi, math.pi/2.0 - oneArcsecInRads)*180.0*3600.0 / math.pi
# ... answer should be 1.0 arcsec

