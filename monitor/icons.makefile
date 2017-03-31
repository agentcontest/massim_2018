all: pngs

pngs: facility_pngs facility_pngs_h agent_pngs agent_pngs_h agent_pngs_i


facility_pngs: www/img/chargingStation.png www/img/resourceNode.png www/img/dump.png www/img/shop.png www/img/storage.png www/img/workshop.png

www/img/chargingStation.png: svg/chargingStation.svg
	inkscape -z -e $@ -w 35 -h 50 $<

www/img/resourceNode.png: svg/resourceNode.svg
	inkscape -z -e $@ -w 35 -h 50 $<

www/img/dump.png: svg/dump.svg
	inkscape -z -e $@ -w 35 -h 50 $<

www/img/shop.png: svg/shop.svg
	inkscape -z -e $@ -w 35 -h 50 $<

www/img/storage.png: svg/storage.svg
	inkscape -z -e $@ -w 35 -h 50 $<

www/img/workshop.png: svg/workshop.svg
	inkscape -z -e $@ -w 35 -h 50 $<


facility_pngs_h: www/img/chargingStation-h.png www/img/resourceNode-h.png www/img/dump-h.png www/img/shop-h.png www/img/storage-h.png www/img/workshop-h.png

www/img/chargingStation-h.png: svg/chargingStation.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

www/img/resourceNode-h.png: svg/resourceNode.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

www/img/dump-h.png: svg/dump.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

www/img/shop-h.png: svg/shop.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

www/img/storage-h.png: svg/storage.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin

www/img/workshop-h.png: svg/workshop.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 35 -h 50 -f /dev/stdin


agent_pngs: www/img/car-a.png www/img/car-b.png www/img/drone-a.png www/img/drone-b.png www/img/motorcycle-a.png www/img/motorcycle-b.png www/img/truck-a.png www/img/truck-b.png www/img/spaceShip-a.png www/img/spaceShip-b.png

www/img/car-a.png: svg/car.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/car-b.png: svg/car.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-a.png: svg/drone.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-b.png: svg/drone.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-a.png: svg/motorcycle.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-b.png: svg/motorcycle.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-a.png: svg/truck.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-b.png: svg/truck.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-a.png: svg/spaceShip.svg
	cat $< | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-b.png: svg/spaceShip.svg
	cat $< | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin


agent_pngs_h: www/img/car-a-h.png www/img/car-b-h.png www/img/drone-a-h.png www/img/drone-b-h.png www/img/motorcycle-a-h.png www/img/motorcycle-b-h.png www/img/truck-a-h.png www/img/truck-b-h.png www/img/spaceShip-a-h.png www/img/spaceShip-b-h.png

www/img/car-a-h.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/car-b-h.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-a-h.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-b-h.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-a-h.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-b-h.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-a-h.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-b-h.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-a-h.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-b-h.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ffffff/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin


agent_pngs_i: www/img/car-a-i.png www/img/car-b-i.png www/img/drone-a-i.png www/img/drone-b-i.png www/img/motorcycle-a-i.png www/img/motorcycle-b-i.png www/img/truck-a-i.png www/img/truck-b-i.png www/img/spaceShip-a-i.png www/img/spaceShip-b-i.png

www/img/car-a-i.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/car-b-i.png: svg/car.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-a-i.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/drone-b-i.png: svg/drone.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-a-i.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/motorcycle-b-i.png: svg/motorcycle.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-a-i.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/truck-b-i.png: svg/truck.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-a-i.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin

www/img/spaceShip-b-i.png: svg/spaceShip.svg
	cat $< | sed -e 's/#000000/#ff7070/g' | sed -e 's/#0072b2/#009e73/g' | inkscape -z -e $@ -w 50 -h 50 -f /dev/stdin
