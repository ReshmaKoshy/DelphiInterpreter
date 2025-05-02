program NestedFunctionDemo;

type
    TPerson = class
    private
        FName: string;
        LName: string;
    public
        constructor Create(const AName: string; const BName: string);
        destructor Destroy; override;
        function GetFname: string;
        function GetLname: string;
    end;

constructor TPerson.Create(const AName: string; const BName: string);
begin
  Writeln('Constructor called: Creating a person object');
  FName := AName;
  LName := BName;
end;

destructor TPerson.Destroy;
begin
  Writeln('Destructor called: Cleaning up the object');
  inherited;
end;

function TPerson.GetFname: string;
begin
  Result := FName;
end;

function TPerson.GetLname: string;
begin
  Result := LName;
end;

function ConcatenateName(const Person : TPerson): string;
var
    LocalFirstName: string;
    LocalLastName: string;
begin
    Writeln('Returning Concatenated name from external function(nested function)');
    LocalFirstName := Person.GetFname();
    LocalLastName := Person.GetLname();
    Result := LocalFirstName + ' ' + LocalLastName ;
end;

var
    Person: TPerson;
    FirstName: string;
    LastName: string;
    Fullname: string;
begin
    Write('Person1: Enter First name: ');
    ReadLn(FirstName);
    Write('Person1: Enter Last name: ');
    ReadLn(LastName);
    Person := TPerson.Create(FirstName, LastName);
    Fullname := ConcatenateName(Person);
    Writeln('Full Name of the Person is ', Fullname);
    Person.Destroy;
end.




