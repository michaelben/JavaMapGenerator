function [xMargin, yMargin, heightMean ,heightStd ,NumberOfRoads, builds,tempD,Road,smRoad,max_height] = create_DBs_height(choice)

close all
clc

if (0)
    xMargin = input('What is the map x-axis length (m) (typically greater than 1000) ? : ');
    yMargin = input('What is the map y-axis length (m) (typically greater than 1000) ? : ');
    roadWidth = input('What is the average street width (>15m) ? : ');
    roadStd = input('What is the street width divergence (m) ? : ');
    heightMean = input('What is the average building height (m) ? : ');
    heightStd = input('What is the building height divergence (m) ? :');
    dist = input('What is the building facade length (m) ? : ');
    number_of_DBs = input('How many random maps do you want to be generated? ');
else
    xMargin     = 1000
    yMargin     = 800
    roadWidth   = 15
    roadStd     = 2
    heightMean  = 7
    heightStd   = 6
    dist        = 30
    number_of_DBs = 2
end;
    
% average_building_area = dist*dist
% dense = area/(average_building_area)
% NumberOfRoads = min(xMargin / average_distance_between_roads, yMargin / average_distance_between_roads) 
% NumberOfRoads = area / average_building_area * average_building_number_per_block 


dense = floor((xMargin+yMargin)/8);    % tyxaia shmeia 
NumberOfRoads = floor((xMargin+yMargin)/200)     %htan 200


[D,ND,Road,wpRoad,smRoad] = generateRoad1(roadWidth,roadStd, NumberOfRoads ,xMargin, yMargin ,dist);

%rand('state',sum(100*clock))
% ----------------------------------------------------------------------- %

if ( xMargin == yMargin)
    B1 = rand(dense,2)*xMargin;
else 
    B1 = rand(dense,2);
    B1(:,1) = B1(:,1)*xMargin;
    B1(:,2) = B1(:,2)*yMargin;
end

% ------------------------------------------------------------------------ %

    

tempD = cell2mat(Road);
wpRoad1 = cell2mat(wpRoad);
% We are checking if the randomly picked points fall into the roads 
% in order to find and render only buildings that do not fall into roads

inD = [];
for i = 1:length(Road)
    inD(:,i) = inpolygon(B1(:,1),B1(:,2),Road{i,1},Road{i,2});
end


for i = 1:length(inD)
    if ( sum(inD(i,:)) == 0 )
        q1(i) = 1;
    else
        q1(i) = 0;
    end
end

L = find( q1 == 1);
% ----------------------------------------------------------------------- %
B = B1(L,:);


figure,hold on
voro_Points = [B ;tempD ;wpRoad1];
[v c] = voronoin(voro_Points);


axis([0 xMargin 0 yMargin])

%- finds roads -%
N = length(B);


%- Here we close open polygons and discard everything that may be outside the map area -%

ind1 = [];
ind2 = [];
ind3 = [];
ind = [];
for i = 1:N
        temp = c{i};
        if (temp(1) ~= temp(end))
            c{i} = [c{i} temp(1)];  % closing polygons
        end
end
           
for i = 1:length(B)+length(tempD)
    
        %if ~( (v(c{i},1) < 0 | v(c{i},1) > xMargin) | (v(c{i},2) < 0 | v(c{i},2) > yMargin) )
         %  V{i,1} = v(c{i},1);
          % V{i,2} = v(c{i},2);
           %ind = [ind i];
           if (sum(( (v(c{i},1) < 0 | v(c{i},1) > xMargin) | (v(c{i},2) < 0 | v(c{i},2) > yMargin) )) == 0)     
           ind = [ind i];
       end
       
 end
 
 for i = 1:N    
        % checking if one or more sides of buildings intersect roads
        %if ( length(VerRoad) == length(HorRoad) )
            for k = 1:length(Road)
                [x1 y1] = polyxpoly( v(c{i},1),v(c{i},2),Road{k,1},Road{k,2} );
                
  
                if ( (isempty([x1 y1])  == 0 ) )% if at least one vector [x1 y1], [x2 y2] is not 'empty' ( [] ) then one or more sides of the building fall into a road
                   % plot(v(c{i},1),v(c{i},2),'g-')                % and the coordinates of this building are set to NaN so it won't be rendered
                    ind1 = [ind1 i];
                    
                end
               
                
            end
            
            
                    
end
% we check if the buildings located close to the main road (Road) fall into the 
% smaller road (smRoad)
for i = length(B)+1:length(B)+length(tempD)
    temp = c{i};
        if (temp(1) ~= temp(end))
            c{i} = [c{i} temp(1)];  % closing polygons
        end
        
    for k = 1:length(smRoad)    
        [x2 y2] = polyxpoly(v(c{i},1),v(c{i},2),smRoad{k,1},smRoad{k,2});
        if (isempty([x2 y2]) == 0)
            %plot(v(c{i},1),v(c{i},2),'r-')
            ind2 = [ind2 i];
        end
    end
end

            
ind1 = [ind1 ind2];

z = [];
for i = 1:length(ind)
    for j = 1:length(ind1)
        if ( ind(i) == ind1(j) )
            z = [z i];
        end
    end
end

ind(z) = [];

% we store all building coordinates into the cell array variable
% builds
for i = 1:length(ind)
     temp = v(c{ind(i)},:);
     builds{i} = temp;
end



% we calculate the area of each building
for i = 1:length(builds)
    temp = builds{i};
    buildArea(i) = polyarea(temp(:,1),temp(:,2));
end




% ----------------------------------------------------------------------- %
% we find the buildings with area greater than a percentage of 
% the maximum building area and we discard them
areaMax = max(buildArea);
areaMin = min(buildArea);
percent1 = 0.6;
percent2 = 3;
index1 = find(buildArea > (percent1*areaMax));
index2 = find(buildArea < (percent2*areaMin));
index = [index1 index2];
tb = builds;
builds = {};
num = 0;
for i = 1:length(tb)
    
    if (sum( i == index ) == 0)
        builds{i-num} = tb{i};
    else
        num = num + 1;
    end
end
buildArea(index) = [];
% ----------------------------- TELOS SXOLIOU --------------------------- %
figure(1),hold on

length(builds)

for i = 1:length(builds)
    temp = builds{i};
    plot(temp(:,1),temp(:,2),'k-')
end
axis image;

pause;

answ = input('do you want to continue? (y/n) : ','s');
if ( answ == 'n')
    return
end


% ----------------------------------------------------------------------- %
% we create a database of variable heights 
% and we assign a height to each building 
h = [];
for k = 1:number_of_DBs

    for i = 1:length(builds)
        % random height calculation using a normal Gaussian distribution with 
        % average value (heightMean) and standard divergence (heightStd) that have been given by the user
    
        % checking to prevent negative values for heights
        height = -10;
        while (height < 6)
            height = normrnd(heightMean,heightStd);  
        end
        h = [h height];
        temp = builds{i};
    
        % we assign a height to each building and store
        % the building coordinates (X, Y, Z) into the cell array "builds"
        temp(:,3) = height*ones(length(temp),1);
        builds{i} = temp;
        
    end

    max_height = max(h);
        % ----------------------------------------------------------------------- %
        %       Here we create the DXF file with the building coordinates         %
        %                 takes as input the cell array "builds"                  %  
        choice=1;
        createDXFile(k,choice,builds,xMargin,yMargin);
        % ----------------------------- TELOS SXOLIOU --------------------------- %
    
end
max_height


figure(2),hold on
for i = 1:length(builds)
    temp = builds{i};
    plot(temp(:,1),temp(:,2),'k-')
end
axis('image')