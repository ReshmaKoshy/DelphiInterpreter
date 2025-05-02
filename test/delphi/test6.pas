program ProcedureDemo;

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

function ConcatenateName(const LocalFirst: string; const LocalLast: string): string;
begin
    Writeln('Returning Concatenated name from external function.');
    Result := LocalFirst + ' ' + LocalLast ;
end;

procedure PrintPersonDetails(const Person: TPerson);
  var
    LocalFullname: string;
    LocalFirstName: string;
    LocalLastName: string;
begin
    LocalFirstName := Person.GetFname();
    LocalLastName := Person.GetLname();
    LocalFullname := ConcatenateName(LocalFirstName, LocalLastName);
    Writeln('Printing Person Details from external procedure: Full name: ', LocalFullname );
end;

var
    Person: TPerson;
    FirstName: string;
    LastName: string;
begin
    Write('Person: Enter First name: ');
    ReadLn(FirstName);
    Write('Person: Enter Last name: ');
    ReadLn(LastName);
    Person := TPerson.Create(FirstName, LastName);
    PrintPersonDetails(Person);
    Person.Destroy;
end.
