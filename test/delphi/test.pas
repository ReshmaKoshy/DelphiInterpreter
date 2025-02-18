program PersonDemo;

type
  TPerson = class
  private
    FName: string;
    LName: string;
  public
    constructor Create(const AName: string; const BName: string);
    destructor Destroy; override;
    function GetFname: string;
    procedure SetFname(const Value: string);
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
  Result := FName + ' ' + LName;
end;

procedure TPerson.SetFname(const Value: string);
begin
  FName := Value;
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
Person1 := TPerson.Create(FirstName, LastName);
Fullname := Person1.GetFname();
Writeln('Full Name of the Person1 is ', Fullname);

Write('Person2: Enter First name: ');
ReadLn(FirstName);
Write('Person2: Enter Last name: ');
ReadLn(LastName);
Person2 := TPerson.Create(FirstName, LastName);
Fullname := Person2.GetFname();
Writeln('Full Name of the Person2 is ', Fullname);

Writeln('Calling destructor for Person1');
Person1.Destroy;
Fullname := Person1.GetFname();
Writeln('Full Name of the Person1 is ', Fullname);
Person2.Destroy;

end
.