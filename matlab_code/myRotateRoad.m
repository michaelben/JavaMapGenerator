function [x,y] = myRotateRoad(x,y,centre)

figure(2); clf; hold on;
title("in myRotateRoad");
plot(x, y, 'b*;blue star before rotate;');

cx= (min(x) + max(x))/2;
cy= (min(y) + max(y))/2;

plot(cx, cy, 'm+;magenta plus center;');

%---- METATOPIZOUME TO SXHMA KATA TO SHMEIO -(cx,cy) ----%
x=x-cx;
y=y-cy;

plot(x, y, 'y*;yellow star after translation by center;');

%---- EPILEGEI TYXAIA MIA GWNIA APTIS 0-360 MOIRES ----%
choice = [-90 -75 -60 -45 -30 -15 0 15 30 45 60 75 90];

ind = randint(1,1,[1:length(choice)]);          %h mhpws rand ari8mos apo -90 ews 90?
th = choice(ind)

%pause;

%---- METATROPH THS GWNIAS APO MOIRES SE RAD ----%
theta=(th*pi)/180.0;

%---- METATOPISH SXHMATOS GYRW APTO SHMEIO (cx,cy) ME GWNIA PERISTROFHS
%---- theta ----%

x1 = x.*cos(theta) - y.*sin(theta);
y1 = x.*sin(theta) + y.*cos(theta);

x = x1;
y = y1;

plot(x, y, 'c+;cyan plus after rotation random degree;');

x=x+cx;
y=y+cy;

plot(x, y, 'r*;red star after translation back by center;');
%pause;