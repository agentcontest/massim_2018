RES=src/main/resources/www/img

all: pngs

pngs: facility_pngs facility_pngs_h agent_pngs agent_pngs_h agent_pngs_i


facility_pngs: $(RES)/chargingStation.png $(RES)/resourceNode.png $(RES)/dump.png $(RES)/shop.png $(RES)/storage.png $(RES)/workshop.png

$(RES)/chargingStation.png: svg/chargingStation.svg
	inkscape -z -e $@ -w 35 -h 50 $<

$(RES)/resourceNode.png: svg/resourceNode.svg
	inkscape -z -e $@ -w 35 -h 50 $<

$(RES)/dump.png: svg/dump.svg
	inkscape -z -e $@ -w 35 -h 50 $<

$(RES)/shop.png: svg/shop.svg
	inkscape -z -e $@ -w 35 -h 50 $<

$(RES)/storage.png: svg/storage.svg
	inkscape -z -e $@ -w 35 -h 50 $<

$(RES)/workshop.png: svg/workshop.svg
	inkscape -z -e $@ -w 35 -h 50 $<


facility_pngs_h: $(RES)/chargingStation-h.png $(RES)/resourceNode-h.png $(RES)/dump-h.png $(RES)/shop-h.png $(RES)/storage-h.png $(RES)/workshop-h.png

$(RES)/chargingStation-h.png: svg/chargingStation.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

$(RES)/resourceNode-h.png: svg/resourceNode.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

$(RES)/dump-h.png: svg/dump.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

$(RES)/shop-h.png: svg/shop.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

$(RES)/storage-h.png: svg/storage.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

$(RES)/workshop-h.png: svg/workshop.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin


agent_pngs: $(RES)/car-a.png $(RES)/car-b.png $(RES)/drone-a.png $(RES)/drone-b.png $(RES)/motorcycle-a.png $(RES)/motorcycle-b.png $(RES)/truck-a.png $(RES)/truck-b.png $(RES)/spaceShip-a.png $(RES)/spaceShip-b.png

$(RES)/car-a.png: svg/car.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/car-b.png: svg/car.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-a.png: svg/drone.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-b.png: svg/drone.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-a.png: svg/motorcycle.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-b.png: svg/motorcycle.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-a.png: svg/truck.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-b.png: svg/truck.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-a.png: svg/spaceShip.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-b.png: svg/spaceShip.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin


agent_pngs_h: $(RES)/car-a-h.png $(RES)/car-b-h.png $(RES)/drone-a-h.png $(RES)/drone-b-h.png $(RES)/motorcycle-a-h.png $(RES)/motorcycle-b-h.png $(RES)/truck-a-h.png $(RES)/truck-b-h.png $(RES)/spaceShip-a-h.png $(RES)/spaceShip-b-h.png

$(RES)/car-a-h.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/car-b-h.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-a-h.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-b-h.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-a-h.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-b-h.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-a-h.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-b-h.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-a-h.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-b-h.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin


agent_pngs_i: $(RES)/car-a-i.png $(RES)/car-b-i.png $(RES)/drone-a-i.png $(RES)/drone-b-i.png $(RES)/motorcycle-a-i.png $(RES)/motorcycle-b-i.png $(RES)/truck-a-i.png $(RES)/truck-b-i.png $(RES)/spaceShip-a-i.png $(RES)/spaceShip-b-i.png

$(RES)/car-a-i.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/car-b-i.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-a-i.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/drone-b-i.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-a-i.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/motorcycle-b-i.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-a-i.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/truck-b-i.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-a-i.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

$(RES)/spaceShip-b-i.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin
