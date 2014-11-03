%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%% myRotateRoad() %%%%%%%%%
% METATOPIZEI TO DROMO GYRW APO TO  %
% SHMEIO (cx,cy) .
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


function [x,y] = myRotateRoad(x,y,centre)

%figure(30); clf; hold on; plot(x, y, 'b*');

cx= (min(x) + max(x))/2;
cy= (min(y) + max(y))/2;

%plot(cx, cy, 'm+');

%---- METATOPIZOUME TO SXHMA KATA TO SHMEIO -(cx,cy) ----%
x=x-cx;
y=y-cy;

%plot(x, y, 'y*');

%---- EPILEGEI TYXAIA MIA GWNIA APTIS 0-360 MOIRES ----%
%th=randint(1,1,[0 360]);
%choice = [0 45 90 135 180 225 270 315 360];
choice = [-90 -75 -60 -45 -30 -15 0 15 30 45 60 75 90];

ind = randint(1,1,[1 length(choice)]);          %h mhpws rand ari8mos apo -90 ews 90?
th = choice(ind)
%pause;

%---- METATROPH THS GWNIAS APO MOIRES SE RAD ----%
theta=(th*pi)/180.0;

%if ( ((th == 45) | (th == -45)) )    
%tmpx = centre.*cos(theta) - centre.*sin(theta)
%tmpy = centre.*sin(theta) + centre.*cos(theta)
%end


%---- METATOPISH SXHMATOS GYRW APTO SHMEIO (cx,cy) ME GWNIA PERISTROFHS
%---- theta ----%

x1 = x.*cos(theta) - y.*sin(theta);
y1 = x.*sin(theta) + y.*cos(theta);

x = x1;
y = y1;

%plot(x, y, 'c+');

x=x+cx;
y=y+cy;

%plot(x, y, 'r*');
%pause;
